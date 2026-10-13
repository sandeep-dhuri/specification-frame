"""Check every third-party import in the study outputs against PyPI.
Usage: python check_dependencies.py results/raw   (from the dataset root)"""
import ast, sys, re, glob, json, collections, urllib.request, os
root = sys.argv[1] if len(sys.argv) > 1 else "results/raw"
ALIAS = {"confluent_kafka": "confluent-kafka", "flask_sqlalchemy": "Flask-SQLAlchemy",
         "kafka": "kafka-python", "flask_login": "Flask-Login"}
stdlib = set(sys.stdlib_module_names)
per_file, counter = {}, collections.Counter()
for f in glob.glob(os.path.join(root, "*", "*.py")):
    src = open(f, encoding="utf-8", errors="ignore").read(); mods = set()
    try:
        for n in ast.walk(ast.parse(src)):
            if isinstance(n, ast.Import): mods |= {a.name.split(".")[0] for a in n.names}
            elif isinstance(n, ast.ImportFrom) and n.module and n.level == 0: mods.add(n.module.split(".")[0])
    except SyntaxError:
        mods |= set(re.findall(r"^\s*(?:from|import)\s+([A-Za-z_]\w*)", src, re.M))
    third = {m for m in mods if m not in stdlib and m != "__future__"}
    per_file[f] = sorted(third); counter.update(third)
results = {}
for m in counter:
    dist = ALIAS.get(m, m)
    try:
        with urllib.request.urlopen(f"https://pypi.org/pypi/{dist}/json", timeout=15) as r: results[m] = (dist, counter[m], r.status == 200)
    except Exception: results[m] = (dist, counter[m], False)
arm = collections.Counter(os.path.basename(f).split("_")[1][0] for f, t in per_file.items() if t)
print(json.dumps({"third_party": results, "files_with_third_party_by_arm": dict(arm),
                  "hallucinated": [m for m, (d, c, ok) in results.items() if not ok]}, indent=2))

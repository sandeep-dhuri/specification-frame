// Delta companion repo - xUnit verification tests.
//   cd csharp && dotnet test
// (Place in a test project referencing the class library: xUnit + FluentAssertions.)
using System.Linq;
using FluentAssertions;
using Xunit;
using YourCompany.Common;
using YourCompany.Domain.ValueObjects;

namespace Delta.Tests;

public class MoneyTests
{
    [Fact]
    public void Add_SameCurrency_Sums()
    {
        var total = new Money<USD>(19.99m).Add(new Money<USD>(2.00m));
        total.ToString().Should().Be("21.99");
    }

    [Fact]
    public void Multiply_RoundsToFourDpThenDisplaysTwo()
    {
        new Money<USD>(10.00m).Multiply(3m).ForOutput().Should().Be(30.00m);
    }

    [Fact]
    public void Divide_HalfUpAtFourDp()
    {
        new Money<USD>(10.00m).Divide(3m).Amount.Should().Be(3.3333m);
    }

    [Fact]
    public void Divide_ByZero_Throws()
    {
        var act = () => new Money<USD>(1m).Divide(0m);
        act.Should().Throw<DivideByZeroException>();
    }

    [Fact]
    public void Equality_IgnoresTrailingZeros()
    {
        new Money<USD>(19.99m).Should().Be(new Money<USD>(19.9900m));
    }

    [Fact]
    public void Allocate_SplitsWithoutLosingAPenny()
    {
        var shares = new Money<USD>(10.00m).Allocate(3);
        shares.Select(s => s.ForOutput()).Should().Equal(3.34m, 3.33m, 3.33m);
        shares.Aggregate(Money<USD>.Zero, (a, s) => a.Add(s)).ForOutput().Should().Be(10.00m);
    }

    [Fact]
    public void Allocate_InvalidParts_Throws()
    {
        var act = () => new Money<USD>(10m).Allocate(0);
        act.Should().Throw<ArgumentOutOfRangeException>();
    }

    // NOTE: the following MUST NOT COMPILE - they are the compile-time guarantees.
    // Uncomment to confirm each produces a compiler error (CS0619 / type mismatch):
    //   var bad1 = new Money<USD>(19.99);              // CS0619 - double poison ctor
    //   var bad2 = new Money<USD>(19.99f);             // CS0619 - float poison ctor
    //   var bad3 = new Money<USD>(1m).Add(new Money<EUR>(1m)); // CS1503 - currency mismatch
}

public class ResultTests
{
    [Fact]
    public void Success_CarriesValue()
    {
        var r = Result<int>.Success(42);
        r.IsSuccess.Should().BeTrue();
        r.Value.Should().Be(42);
    }

    [Fact]
    public void Failure_GetValueOrThrow_Throws()
    {
        var r = Result<int>.Failure("CODE", "msg");
        var act = () => r.GetValueOrThrow();
        act.Should().Throw<InvalidOperationException>();
    }

    [Fact]
    public void Map_Success_Transforms()
    {
        Result<int>.Success(21).Map(x => x * 2).Value.Should().Be(42);
    }

    [Fact]
    public void Map_Failure_PassesThrough()
    {
        Result<int>.Failure("E", "m").Map(x => x * 2).ErrorCode.Should().Be("E");
    }

    [Fact]
    public void Success_Null_Throws()
    {
        var act = () => Result<string>.Success(null!);
        act.Should().Throw<ArgumentNullException>();
    }
}

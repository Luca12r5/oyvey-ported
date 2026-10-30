using Duelcliping.Core.Library;

namespace Duelcliping.Core.Tests;

public class CutPlanTests
{
    private static TimeSpan S(double s) => TimeSpan.FromSeconds(s);

    [Fact]
    public void Split_and_remove_middle_produces_two_segments()
    {
        var plan = new CutPlan(S(60));
        Assert.True(plan.Split(S(10)));
        Assert.True(plan.Split(S(20)));
        plan.SetKeep(plan.IndexAt(S(15)), false);
        Assert.Equal(new[] { new CutSegment(S(0), S(10)), new CutSegment(S(20), S(60)) }, plan.ToSegments());
        Assert.Equal(S(50), plan.OutputDuration);
        Assert.Equal(S(20), plan.SkipRemoved(S(12)));
        Assert.Equal(S(5), plan.SkipRemoved(S(5)));
    }

    [Fact]
    public void Splits_without_removal_merge_back_into_one_segment()
    {
        var plan = new CutPlan(S(30));
        plan.Split(S(5));
        plan.Split(S(9));
        Assert.Single(plan.ToSegments());
        Assert.False(plan.Split(S(5.05))); // too close to an existing cut
    }

    [Fact]
    public void Mark_in_and_out_trim_both_ends()
    {
        var plan = new CutPlan(S(100));
        plan.RemoveBefore(S(12));
        plan.RemoveAfter(S(40));
        Assert.Equal(new[] { new CutSegment(S(12), S(40)) }, plan.ToSegments());
        Assert.Null(plan.SkipRemoved(S(50)));
    }

    [Fact]
    public void Undo_and_redo_restore_previous_states()
    {
        var plan = new CutPlan(S(20));
        plan.Split(S(10));
        plan.SetKeep(1, false);
        Assert.Equal(S(10), plan.OutputDuration);
        plan.Undo();
        Assert.Equal(S(20), plan.OutputDuration);
        plan.Undo();
        Assert.Single(plan.Pieces);
        plan.Redo();
        Assert.Equal(2, plan.Pieces.Count);
        Assert.False(plan.CanRedo == false && plan.CanUndo == false);
    }

    [Fact]
    public void Cannot_remove_everything()
    {
        var plan = new CutPlan(S(20));
        Assert.Throws<InvalidOperationException>(() => plan.SetKeep(0, false));
        plan.RemoveBefore(S(10));
        Assert.Throws<InvalidOperationException>(() => plan.RemoveAfter(S(5)));
    }
}

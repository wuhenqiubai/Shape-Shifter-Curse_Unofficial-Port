package net.onixary.shapeShifterCurseFabric.util;

public class ServerTicker implements ServerTickable {
    private final Runnable task;
    private int ticksRemaining;
    private final boolean runOnce;

    /**
     * @param task         每 tick 执行的任务（runOnce=false时）或延时后执行一次的任务（runOnce=true时）
     * @param durationTicks 持续时间（tick 数），20 ticks = 1 秒
     * @param runOnce       true = 延时后只执行一次（替代 Thread.sleep），false = 每 tick 执行一次（原行为）
     */
    public ServerTicker(Runnable task, int durationTicks, boolean runOnce) {
        this.task = task;
        this.ticksRemaining = durationTicks;
        this.runOnce = runOnce;
    }

    /** 向后兼容：默认 runOnce=false，每 tick 执行 */
    public ServerTicker(Runnable task, int durationTicks) {
        this(task, durationTicks, false);
    }

    @Override
    public void tick() {
        if (ticksRemaining > 0) {
            if (!runOnce) {
                task.run();
            }
            ticksRemaining--;
            if (runOnce && ticksRemaining == 0) {
                task.run();
                TickManager.removeTickable(this);
            }
        } else {
            TickManager.removeTickable(this);
        }
    }

    public void start() {
        TickManager.addTickable(this);
    }
}

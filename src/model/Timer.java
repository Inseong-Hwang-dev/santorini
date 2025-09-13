package model;

/**
 * Timer class to manage countdown for each player.
 */
public class Timer {
    private long playerTimes; // Remaining time in milliseconds
    private long lastStartTime;  // Last time the timer was started
    private boolean isRunning;

    /**
     * Constructor to initialize the timer with a given time.
     * @param initialTime Initial time in milliseconds
     */
    public Timer(long initialTime) {
        this.playerTimes = initialTime;
        this.isRunning = false;
    }

    /**
     * Start or resume the timer.
     */
    public void start() {
        if (!isRunning) {
            lastStartTime = System.currentTimeMillis();
            isRunning = true;
        }
    }

    /**
     * Pause the timer and update the remaining time.
     */
    public void pause() {
        if (isRunning) {
            long now = System.currentTimeMillis();
            playerTimes -= (now - lastStartTime);
            isRunning = false;
        }
    }

    /**
     * Reset the timer to a new time value.
     * @param newTimeMillis New time in milliseconds
     */
    public void reset(long newTimeMillis) {
        this.playerTimes = newTimeMillis;
        this.isRunning = false;
    }

    /**
     * Get the remaining time in milliseconds.
     * @return Remaining time in milliseconds
     */
    public long getRemainingTime() {
        if (isRunning) {
            long now = System.currentTimeMillis();
            return playerTimes - (now - lastStartTime);
        }
        return playerTimes;
    }

    /**
     * Check if the timer has run out.
     * @return true if time is over, false otherwise
     */
    public boolean isPlayerTimerExpired() {
        return getRemainingTime() <= 0;
    }

    /**
     * Check if the timer is currently running.
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
} 
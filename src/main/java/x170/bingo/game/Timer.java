package x170.bingo.game;

public class Timer {
    private static int time = 0;
    private static boolean paused = true;

    public static String getTime() {
        if (time == 0) {
            return "0s";
        }

        int hours = time / 3600;
        int minutes = time / 60 % 60;
        int seconds = time % 60;

        String timeString = "";
        if (hours > 0) timeString += hours + "h ";
        if (minutes > 0) timeString += minutes + "m ";
        timeString += seconds + "s";

        if (paused) timeString += " (paused)";

        return timeString;
    }

    public static void start() {
        time = 0;
        paused = false;
    }

    public static String stop() {
        String timeString = getTime();
        time = 0;
        paused = true;
        return timeString;
    }

    public static void pause() {
        paused = true;
    }

    public static void resume() {
        paused = false;
    }

    public static void tick() {
        if (!paused) {
            time++;
        }
    }
}

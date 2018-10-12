package ca.uvic.concurrency.gmmurguia.a1;

public class ExecutableSolution {

    public static boolean randomize = true;
    public static int repetitions = 100;

    public static void main(String[] args) {
        if (args.length > 0) {
            randomize = Boolean.valueOf(args[0]);
            repetitions = Integer.valueOf(args[1]);
        }
    }
}

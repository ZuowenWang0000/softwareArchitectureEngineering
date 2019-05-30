public class Example4 {
    public static Car m1(int j) {
        Car b = new Car();
        if (j == 2*j + 1) {
            b.setSpeed(j);
        } else {
            b.setSpeed(-2);
        }
        Car c = b;
        return c;
    }
}
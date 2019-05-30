public class Example1 {
    public static Car test(int i) {	
        Car car = new Car();
        if (i < 5) {
            car.setSpeed(i);
        }
        return car;
    }
}
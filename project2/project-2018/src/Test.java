
public class Test {
//	public static Car testNull() {
//		return null;
//	}
//	
//	public static Car testNewCar() {
//		return new Car();
//	}
	
	public static Car testNotEqual(int i) {
		int j = 2;
		Car car = new Car();
		// j < 2 || j > 2
		int x = 2;
		int y = 4;
		if (x == y - x) {
			car.setSpeed(0);
			return car;
		} else {
			car.setSpeed(3);
			return car;
		}
	}
	
	public static Car test23() {
		Car car = new Car();
		car.setSpeed(Integer.MIN_VALUE);
		return car;
	}
	
	public static Car test2(int i) {
		int j; int g;
		Car car = new Car();
		j = (i < 5) ? 3 : 5;
		g = (i < 5) ? 4 : 6;
		
		if (j != g) {
			car.setSpeed(j);
		} else {
			car.setSpeed(-1);
		}
		return car;
	}
	
	public  static Car test21(int i) {
		int j; int g;
		Car car = new Car();
		j = (i < 5) ? 2 : 6;
		g = (i < 5) ? 4 : 8;
		
		if (j != g) {
			car.setSpeed(j);
		} else {
			car.setSpeed(123);
		}
		return car;
	}
	
	public static Car test3(int i) {
		Car car = new Car();
		int j = 1;
		if (j != 2) {
			car.setSpeed(3);
		} else {
			car.setSpeed(4);
		}
		return car;
	}
	
	public static Car test4(int i) {
		Car car = new Car();
		if (i > Integer.MAX_VALUE) {
			car.setSpeed(i);
		}
		return car;
//		int j = (i < 5) ? 
	}
	
	public static Car test399(int i) {
		// It should output 2
		Car car = new Car();
		int j = 3;
		if (j == 2) {
			car.setSpeed(2);
		} else if (j == 3) {
			car.setSpeed(3);
		} else if (j < 4) {
			car.setSpeed(1);
		} 
		return car;
	}
	
	public static Car test3939(int i) {
		int j = (i < 5) ? 3 : 4;
		int k = (i < 5) ? 2 : 5;
		Car car = new Car();
		if (j == k) {
			car.setSpeed(2);
		} else {
			car.setSpeed(1);
		}
		return car;
	}
	
	public static Car test039349(int i) {
		Car car = new Car();
		int j = (i < 5) ? 1 : 2;
		int k = (i < 5) ? 3 : 4;
		if (j == k) { // This should be impossible, so it must execute the else branch
			car.setSpeed(3);
		} else {  // In other words, if (j == k), which is impossible, so it doesn't go here
			car.setSpeed(4);
		}
		return car;
	}
	
	public static Car testReinitialize() {
		Car car = new Car();
		car.setSpeed(Integer.MIN_VALUE);
		car = new Car();
		car.setSpeed(2);
		return car;
	}
	
	public static Car test9340(int i) {
		Car car = new Car();
		if (i < 2 * Integer.MAX_VALUE) {
			car.setSpeed(i);
		}
		return car;
	}
	
	public static Car testMin(int i) {
		Car car = new Car();
		if (i < Integer.MIN_VALUE) {
			car.setSpeed(i);
		} else if (Integer.MAX_VALUE < i) {
			car.setSpeed(i);
		}
		return car;
	}
	
	public static Car testMax(int i) {
		Car car = new Car();
		if (i > Integer.MAX_VALUE) {
			car.setSpeed(i);
		} else if (Integer.MIN_VALUE > i) {
			car.setSpeed(i);
		}
		return car;
	}
	
	public static Car test3993(int i) {
		Car car = new Car();
		int j = (i < 5) ? 5 : Integer.MAX_VALUE;
		if (i <= j) {
			car.setSpeed(5);
		} else {
			car.setSpeed(43);
		}
		return car;
	}
	
	public static Car test93939(int i) {
		Car car = new Car();
		if (i >= Integer.MIN_VALUE) {
			
		} else {
			car.setSpeed(i);
		}
		return car;
	}
	
	public static Car problem(int i) {
		Car car = new Car();
		Car car2;
		car.setSpeed(10);
		if (i < 5) {
			car2 = car;
		} else {
			car2 = new Car();
		}
		car2.setSpeed(5);
		return car;
	}
	
	public static Car problem2(int i) {
		Car car = new Car();
		Car car2;
		car.setSpeed(10);
		if (i < 5) {
			car2 = car;
		} else {
			car2 = new Car();
		}
		car2.setSpeed(15);
		return car;
	}
	
	public static Car problem3(int i) {
		Car car = new Car();
		Car car2;
		car.setSpeed(100000);
		if (i < 5) {
			car2 = car;
		} else {
			car2 = new Car();
		}
		car2.setSpeed(1);
		return car2;
	}
	
	public static Car problem4(int i) {
		Car car = new Car();
		car.setSpeed(10);
		Car car2 = new Car();
		car2 = car;
		car2.setSpeed(1);
		car2 = new Car();
		return car;
	}
}

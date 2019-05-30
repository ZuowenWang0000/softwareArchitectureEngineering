package ch.ethz.sae;

import apron.*;

/* no need to change this class */

/* wrap around an Apron abstract element;  */
public class AWrapper {

	/* Apron abstract element */
	Abstract1 elem;

	/* Apron abstract domain manager */
	Manager man;

	/* constructor */
	public AWrapper(Abstract1 e) {
		elem = e;
	}

	public Abstract1 get() {
		return elem;
	}

	public void set(Abstract1 e) {
		elem = e;
	}

	public void copy(AWrapper src) {
		this.elem = src.get();
	}

	@Override
	public boolean equals(Object o) {
		Abstract1 t = ((AWrapper) o).get();
		try {
			return elem.isIncluded(man, t);
		} catch (ApronException e) {
			System.err.println("isEqual failed");
			System.exit(-1);
		}
		return false;
	}

	@Override 
	public String toString() {
		try {
			if (elem.isTop(man))
				return "<Top>";

			return elem.toString();
		} catch (ApronException e) {
			System.err.println("toString failed");
			System.exit(-1);
		}
		return null;
	}
}

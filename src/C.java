import net.higher_order.integration.circuit_breaker.java.AtomicCircuitBreaker;
import net.higher_order.integration.circuit_breaker.java.CircuitBreaker;
import clojure.lang.IFn;
import clojure.lang.RT;



public class C {
	public static void main(String[] args) {
		CircuitBreaker atomicCircuitBreaker = new AtomicCircuitBreaker();
		IFn wrap = (IFn) atomicCircuitBreaker.wrap(new clojure.lang.AFn() {
			public Object invoke(Object arg0) throws Exception {
				if (arg0 == null) throw new IllegalArgumentException("null arg");
				System.out.println("Invoked with: "+arg0);
				return arg0;
			}
		});
		succeed(atomicCircuitBreaker, wrap);
		fail(atomicCircuitBreaker, wrap);
		fail(atomicCircuitBreaker, wrap);
		fail(atomicCircuitBreaker, wrap);
		fail(atomicCircuitBreaker, wrap);
		fail(atomicCircuitBreaker, wrap);
		fail(atomicCircuitBreaker, wrap);
		sleep(1000);
		status(atomicCircuitBreaker);
		fail(atomicCircuitBreaker, wrap);
		sleep(5000);
		succeed(atomicCircuitBreaker, wrap);
	}

	
	private static void sleep(long howlong) {
		try {
			Thread.sleep(howlong);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void succeed(CircuitBreaker atomicCircuitBreaker, IFn wrap) {
		try {
			System.out.println(wrap.invoke("KARL"));
			System.out.println(wrap.invoke(42));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			status(atomicCircuitBreaker);
		}
	}


	private static void status(CircuitBreaker atomicCircuitBreaker) {
		System.out.println(RT.printString(atomicCircuitBreaker.getCurrentState()));
	}

	private static void fail(CircuitBreaker atomicCircuitBreaker, IFn wrap) {
		try {
			System.out.println(wrap.invoke(null));
			System.out.println(wrap.invoke(42));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			status(atomicCircuitBreaker);
		}
	}
}

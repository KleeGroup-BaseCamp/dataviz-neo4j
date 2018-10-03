package data_Tools;

import java.util.ArrayList;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;

public class Polynomial_Solver {
	
	 public static Double solver(ArrayList<Double> coeff,Double value) {
	        BrentSolver solver = new BrentSolver();
	        UnivariateFunction f;
	        if(coeff.size()==3) {
	        	f = new UnivariateFunction() {
	        		@Override
		            public double value(double x) {
		                return coeff.get(2)*Math.pow(x, 2.0)+coeff.get(1)*x+(coeff.get(0)-value);
		            }
		        };
	        }
	        else {
	        		f = new UnivariateFunction() {
		            @Override
		            public double value(double x) {
		                return coeff.get(3)*Math.pow(x, 3.0)-coeff.get(2)*Math.pow(x, 2.0)+coeff.get(1)*x+(coeff.get(0)-value);
		            }
		        };
	        }
	        double intervalStart = 0;
	        double intervalSize = 0.01;
	        while (intervalStart < 100) {
	            intervalStart+= intervalSize;
	            if(Math.signum(f.value(intervalStart)) != Math.signum(f.value(intervalStart+intervalSize))) {
	                return solver.solve(1000, f, intervalStart, intervalStart+intervalSize);
	            }
	        }
	        return null;
	    }
}

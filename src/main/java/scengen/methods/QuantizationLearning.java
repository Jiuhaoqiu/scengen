package scengen.methods;


import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import scengen.distribution.MultivariateDistribution;
import scengen.tools.Metrics;


public class QuantizationLearning extends ReductionMethod {
	
	MultivariateDistribution _mvdist;
	
	public QuantizationLearning (MultivariateDistribution dist) {
		_mvdist = dist;
	}
	
	@Override
	public Map<double[], Double> getScenarios(int numScen) {
		int dim = _mvdist.getDim();
		double a = 100*numScen;
		int sampleSize = 10000*numScen; 
		double[][] scenarios = new double[numScen][];
		double[] probs = new double[numScen];
		Arrays.fill(probs, 1);
		MonteCarlo rand = new MonteCarlo(_mvdist);
		Iterator<double[]> iter = rand.getScenarios(numScen).keySet().iterator();
		for (int j=0; j<numScen; j++) 
			scenarios[j] = iter.next();
		for (int i=0; i<sampleSize; i++) {
			double[] point = _mvdist.getRealization();
			int nearest = 0;
			double minDist = Double.MAX_VALUE;
			for (int j=0; j<numScen; j++) {
				double dist = Metrics.squaredDistance(scenarios[j], point);
				if (dist<minDist) {
					minDist = dist;
					nearest = j;
				}
			}
			for (int d=0; d<dim; d++) 
				scenarios[nearest][d] += a/(a+i+1)*(point[d]-scenarios[nearest][d]);
			for (int j=0; j<numScen; j++) {
				if (j==nearest)
					probs[j] += (1.-probs[j])/(i+1.);
				else
					probs[j] -= probs[j]/(i+1.);
			}
    	}
		Map<double[],Double> map = new LinkedHashMap<>();
		for (int j=0; j<numScen; j++) 
			map.put(scenarios[j],probs[j]);
		return map;
	}

}

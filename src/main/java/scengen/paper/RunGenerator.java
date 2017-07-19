package scengen.paper;

import java.util.Map;
import java.util.Random;

import scengen.distribution.DISTRIBUTION;
import scengen.distribution.MultivariateDistribution;
import scengen.distribution.MultivariateLognormal;
import scengen.distribution.MultivariateNormal;
import scengen.distribution.MultivariateStudent;
import scengen.distribution.MultivariateUniform;
import scengen.methods.METHOD;
import scengen.methods.MomentMatching;
import scengen.methods.MonteCarlo;
import scengen.methods.QuantizationGrids;
import scengen.methods.QuantizationLearning;
import scengen.methods.QuasiMonteCarlo;
import scengen.methods.ReductionMethod;
import scengen.methods.Scenred2;
import scengen.methods.VoronoiCellSampling;


public class RunGenerator {

	public static void main(String... args) {
		int dim = 2;
		int numScen = 10;
		print(makeScen(dim,numScen,DISTRIBUTION.Normal,METHOD.MomentMatching,new Random()),dim);
	}

	public static Map<double[],Double> makeScen(int dim, int numScen, DISTRIBUTION dist, METHOD method, Random rand) {
		double[] mean = new double[dim];
		double[] scale = new double[dim];
		int[] df = new int[dim];
		double[][] cov = new double[dim][dim];
		for (int i=0; i<dim; i++) {
			mean[i] = dist==DISTRIBUTION.Lognormal?1:0;
			scale[i] = 1;
			df[i] = 5;
			cov[i][i] = 0.5;
		}
		MultivariateDistribution mvdist = null;
		ReductionMethod pb = null;
		switch (dist) {
		case Normal: mvdist = new MultivariateNormal(mean,cov,rand); 
			break;
		case Lognormal: mvdist = new MultivariateLognormal(mean,cov,rand);
			break;
		case Uniform: mvdist = new MultivariateUniform(mean,cov,rand);
			break;
		case Student: mvdist = new MultivariateStudent(mean,scale,cov,df,rand);
			break;
		default:
				break;
		}
		switch (method) {
		case MomentMatching: pb = new MomentMatching(mvdist.getMean(),mvdist.getCov(),mvdist.getSkewness(),mvdist.getKurtosis(),rand.nextLong());
			break;
		case MonteCarlo: pb = new MonteCarlo(mvdist);
			break;
		case QuantizationLearning: pb = new QuantizationLearning(mvdist);
			break;
		case QuasiMonteCarlo: pb = new QuasiMonteCarlo(mvdist);
			break;
		case VoronoiCellSampling: pb = new VoronoiCellSampling(mvdist);
			break;
		case QuantizationGrids: pb = new QuantizationGrids(mvdist);
			break;
		default:
			throw new IllegalArgumentException();
		}
		return pb.getScenarios(numScen);
		
	}
	
	private static void print(Map<double[], Double> scen, int dim) {
		StringBuilder sb = new StringBuilder();
			sb.append("probability");
			for (int i=0; i<dim; i++)
				sb.append(String.format("\tvalue(%d)",i));
			sb.append("\n");
			for (double[] x : scen.keySet()) {
				sb.append(String.format("%f",scen.get(x)));
				for (int i=0; i<dim; i++)
					sb.append(String.format("\t%f",x[i]));
				sb.append("\n");
			}
			System.out.println(sb.toString());
	}

}

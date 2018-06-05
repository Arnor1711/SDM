package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import data.DataPoint;
import data.DataSet;
import kMeans.Cluster;
import kMeans.KMeans;

public class KMain {

	public static void main(String[] args) {
		
		
		/*
		
		double[] c1= {34, 83, 51, 21, 93, 8, 10, 17, 18, 94};
		double[] c2= {0, 21, 22, 50, 5, 75, 12, 24, 94, 9};
		double[] c3= {48, 19, 17, 85, 42, 83, 25, 72, 62, 20};
		double[] c4= {44, 58, 58, 60, 52, 25, 43, 51, 73, 42};
		double[] c5= {39, 33, 20, 55, 53, 46, 49, 56, 20, 30};
		double[] c6= {6, 77, 6, 64, 46, 46, 37, 64, 4, 98};
		double[] c7= {71, 35, 6, 0, 48, 18, 64, 94, 52, 48};
		double[] c8= {91, 62, 75, 6, 90, 51, 1, 55, 71, 67};
		double[] c9= {51, 78, 10, 4, 88, 74, 61, 85, 51, 34};
		double[] c10= {34, 37, 57, 31, 49, 61, 61, 37, 58, 64};
		double[] c11= {16, 59, 78, 65, 45, 17, 83, 79, 11, 41};
		double[] c12= {73, 25, 34, 60, 47, 31, 16, 36, 22, 75};
		double[] c13= {20, 14, 12, 42, 38, 10, 47, 30, 28, 6};
		double[] c14= {84, 84, 51, 5, 48, 42, 25, 45, 17, 48};
		double[] c15= {68, 61, 60, 67, 43, 70, 62, 50, 49, 49};
		
		ArrayList<DataPoint> startPoint=new ArrayList<>();
		
		startPoint.add(new DataPoint(c1,null));
		startPoint.add(new DataPoint(c2,null));
		startPoint.add(new DataPoint(c3,null));
		startPoint.add(new DataPoint(c4,null));
		startPoint.add(new DataPoint(c5,null));
		startPoint.add(new DataPoint(c6,null));
		startPoint.add(new DataPoint(c7,null));
		startPoint.add(new DataPoint(c8,null));
		startPoint.add(new DataPoint(c9,null));
		startPoint.add(new DataPoint(c10,null));
		startPoint.add(new DataPoint(c11,null));
		startPoint.add(new DataPoint(c12,null));
		startPoint.add(new DataPoint(c13,null));
		startPoint.add(new DataPoint(c14,null));
		startPoint.add(new DataPoint(c15,null));
		*/
		
		//startPoint.addAll({p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15});
		
		//ArrayList<DataPoint> startPoint=getRandomStartPoints(15,10);
		KMeans kmeans;
		if(args.length>0&&args[0].equals("normal")) kmeans=new KMeans(true);	//without hash
		else kmeans=new KMeans(false);		//with hash
		
		DataSet dataSet=new DataSet("LSH-nmi.csv");
		List<DataPoint> data=dataSet.getDataPoints();
		
		ArrayList<DataPoint> startPoint=getRandomPoints(15,10,data);
														
		int ands=4;
		int ors=4;
		int bucketSize=1000;
		int iterations=10;
		
		
		
		
		TimeMeasurement time=new TimeMeasurement();
			
		time.Start();
			ArrayList<Cluster> clusters=kmeans.lshLloyed(startPoint, data, ands, ors ,bucketSize, iterations);	//ANDs,ORs,buckets,iterations
		time.Stop();
			for(int i=0;i<clusters.size();i++) {
				for(DataPoint point: clusters.get(i).getDataPoints()) {
					point.setCluster(i);
				}
			}
			
			
			NMI(dataSet.getTruthCluster(), dataSet.getCluster());
		System.out.print("Time for KMeans was " + time.get() + " Miliseconds");
	}
	
	
	private static ArrayList<DataPoint> getRandomStartPoints(int i, int dim) {
		ArrayList<DataPoint> ret=new ArrayList<DataPoint>();
		
		Random random=new Random();
		
		for(int j=0;j<i;j++) {
			double[] vec=new double[dim];
			for(int k=0;k<dim;k++) {
				vec[k]=random.nextDouble()*100;
			}
			ret.add(new DataPoint(vec, null));
		}
		return ret;
	}
	
	private static ArrayList<DataPoint> getRandomPoints(int i,int dim, List<DataPoint> data){
		ArrayList<DataPoint> ret=new ArrayList<DataPoint>();
		Random random=new Random();
		
		ArrayList<Integer> used=new ArrayList<Integer>();
		
		for(int j=0;j<i;j++) {
			int rand=random.nextInt(data.size());
			
			while(used.contains(rand)) {
				rand=random.nextInt(data.size());
			}
			
			ret.add(new DataPoint(data.get(rand).getVector(),null));
			used.add(rand);
		}
		
		
		return ret;
	}


	public static double NMI(ArrayList<Integer> one, ArrayList<Integer> two){
		if(one.size()!=two.size()){
			throw new IllegalArgumentException("Sizes don't match!");
		}
		int maxone = Collections.max(one);
		int maxtwo = Collections.max(two);

		double[][] count = new double[maxone+1][maxtwo+1];
		//System.out.println(count[1][2]);
		for(int i=0;i<one.size();i++){
			count[one.get(i)][two.get(i)]++;
		}
		//i<maxone=R
		//j<maxtwo=C
		double[] bj = new double[maxtwo+1];
		double[] ai = new double[maxone+1];

		for(int m=0;m<(maxtwo+1);m++){
			for(int l=0;l<(maxone+1);l++){
				bj[m]=bj[m]+count[l][m];
			}
		}
		for(int m=0;m<(maxone+1);m++){
			for(int l=0;l<(maxtwo+1);l++){
				ai[m]=ai[m]+count[m][l];
			}
		}

		double N=0;
		for(int i=0;i<ai.length;i++){
			N=N+ai[i];
		}
		double HU = 0;
		for(int l=0;l<ai.length;l++){
			double c=0;
			c=(ai[l]/N);
			if(c>0){
				HU=HU-c*Math.log(c);
			}
		}

		double HV = 0;
		for(int l=0;l<bj.length;l++){
			double c=0;
			c=(bj[l]/N);
			if(c>0){
				HV=HV-c*Math.log(c);
			}
		}
		double HUstrichV=0;
		for(int i=0;i<(maxone+1);i++){
			for(int j=0;j<(maxtwo+1);j++){
				if(count[i][j]>0){
					HUstrichV=HUstrichV-count[i][j]/N*Math.log(((count[i][j])/(bj[j])));
				}
			}
		}
		double IUV = HU-HUstrichV;
		double reto = IUV/(Math.max(HU, HV));

		System.out.println("NMI: "+reto);
		return reto;
	}

}

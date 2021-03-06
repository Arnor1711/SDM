package Hashfunktions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.DataPoint;

public class LSH implements Hasher {

	private double[] p;
	private double bucketSize;
	private int bucketNumber;
	private Bucket[] buckets;
	private Map<DataPoint, Double> hashValues;
	private double minimum;
	private double maximum;
	
	public LSH(double[] p, int bucketNumber) {
		setP(p);
		setBucketNumber(bucketNumber);
		hashValues = new HashMap<>();
	}
	
	public Bucket[] getBuckets() {
		return buckets;
	}
	
	public void setP(double[] p) {
		this.p = p;
	}
	
	public double[] getP() {
		return p;
	}
	
	public void setBucketNumber(int bucketNumber) {
		this.bucketNumber = bucketNumber;
	}
	
	public int getBucketNumber() {
		return bucketNumber;
	}
	
	public void setBucketSize(double bucketSize) {
		this.bucketSize = bucketSize;
	}
	
	public double getBucketSize() {
		return bucketSize;
	}
	
	@Override
	public void hash(List<DataPoint> dataPoints) {
		
		getMinimumAndMaximum(dataPoints);
		constructBuckets();
		int index;
		for (int i = 0; i < dataPoints.size(); ++i) {

			index = (int) ((hashValues.get(dataPoints.get(i)) - minimum) / getBucketSize());
			buckets[index].getHashValuesMap().put(dataPoints.get(i), calcHash(dataPoints.get(i)));
			dataPoints.get(i).setCluster(index);
		}
	}
	
	public List<DataPoint> getPointsByCentroid(DataPoint point){

		int index=(int) ((calcHash(point)-minimum)/getBucketSize());
		return buckets[index].getDataPoints();	
	}

	public void combineHashOR(List<DataPoint> dataPoints, LSH otherHasher) {

		// TODO defensive programming: number of buckets must be equal
		getMinimumAndMaximum(dataPoints);
		constructBuckets();
		int index;
		int otherHashIndex;
		for (int i = 0; i < dataPoints.size(); ++i) {

            index = (int) ((hashValues.get(dataPoints.get(i)) - minimum) / getBucketSize());
            buckets[index].getHashValuesMap().put(dataPoints.get(i), calcHash(dataPoints.get(i)));
            otherHashIndex = (int) ((otherHasher.hashValues.get(dataPoints.get(i)) - otherHasher.minimum) / otherHasher.getBucketSize());
            if (index != otherHashIndex) {

                buckets[otherHashIndex].getHashValuesMap().put(dataPoints.get(i), otherHasher.calcHash(dataPoints.get(i)));
                dataPoints.get(i).setCluster(index);
            }
        }
	}

	public void combineHashAND(List<DataPoint> dataPoints, LSH otherHasher) {

		// TODO defensive programming: number of buckets must be equal
		getMinimumAndMaximum(dataPoints);
		constructBuckets();
		int index;
		int otherHashIndex;
		for (int i = 0; i < dataPoints.size(); ++i) {

			index = (int) ((hashValues.get(dataPoints.get(i)) - minimum) / getBucketSize());
			otherHashIndex = (int) ((otherHasher.hashValues.get(dataPoints.get(i)) - otherHasher.minimum) / otherHasher.getBucketSize());
			if (index == otherHashIndex) {
				buckets[index].getHashValuesMap().put(dataPoints.get(i), calcHash(dataPoints.get(i)));
				dataPoints.get(i).setCluster(index);
			}
		}
	}
	
	private void constructBuckets() {
		
		// +1 to deal with possible round-up errors
		double range = maximum - minimum + 1;
		setBucketSize(range / bucketNumber);
		buckets = new Bucket[getBucketNumber()];
		for (int i = 0; i < getBucketNumber(); ++i) {
			buckets[i] = new Bucket();
		}
	}
	
	private void getMinimumAndMaximum(List<DataPoint> dataPoints) {
		
		double value = calcHash(dataPoints.get(0));

		minimum = maximum = value;
		for (DataPoint dataPoint : dataPoints) {
			value = calcHash(dataPoint);
			hashValues.put(dataPoint, value);
			if (value < minimum) {
				minimum = value;
			}
			if (value > maximum) {
				maximum = value;
			}
		}
	}
	
	private double calcHash(DataPoint dataPoint){

		double value=0.;
		for (int i = 0; i < p.length; ++i) {
			value += dataPoint.getVector()[i] * p[i];
		}
		return value;
	}
	
}




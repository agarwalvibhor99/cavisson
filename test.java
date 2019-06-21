import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import java.util.*;
import java.lang.Math.*;
import java.io.File;
import java.lang.*;
import java.io.*;

public class speedIndex {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	public static int clipTimeIndex(String path){


		int index;
		String subPart[] = null;
		File f = new File(path);
		//File [] fileArr = f.listFiles();

		for (File file : f.listFiles()) {
			String fileName = file.getName();
			subPart = fileName.split("_");
		}
		index = Integer.parseInt(subPart[12]);

		return index;
	}
	//absolute path name
	//get name


	public static int countFiles(String path) {
		File f = new File(path);
		File [] fileArr = f.listFiles();
		return fileArr.length;
    /*int count = 0;
      for (File file : f.listFiles()) {
      if (file.isFile() && file.getName().endsWith(".jpg")) {
      count++;
      }
      }
      System.out.println("Number of files: " + count);
      return count;*/
	}

	public static int calSpeedIndex(String path) {
		int i = 0, maxTime = 0;
		double speedIndexVal[] = new double[countFiles(path)]; // what size of the double array num of images
		double intervalScore = 0.0, sumSpeedIndex = 0.0;
		int noOfSnapshots = countFiles(path);
		int compare_method = 3;
		int rebaseFlag = 0;
		int retryCount = 3;
		int rebaseIndex = 0;
		int speedIndex = 0;
		File f = new File(path);

		File [] fileArr = f.listFiles();
		if (countFiles(path) == 1) {
			return 0;
		}


		do {
			//Imgcodecs imageCodecs = new Imgcodecs();
			Mat srcFirst, hsvFirst = new Mat();
			Mat srcLast, hsvLast = new Mat();

			// Histograms
			//  Mat hist_first = new Mat();
			// Mat hist_last = new Mat();
			String firstImage = null, lastImage = null;
			// File f = new File(path);
			// File [] fileArr = f.listFiles();

			for (File file : f.listFiles()) {
				String fileName = file.getName();
				String subPart[] = fileName.split("_");
				System.out.println(Arrays.toString(subPart));
				System.out.println(subPart[12]);
//      System.out.println("Helllooo!!!!"+fileName);
				if (subPart[12].equals("0")){
//        System.out.println("Helllooo!!!!"+fileName);
					firstImage = file.getAbsolutePath();
				}
				else if(fileName.endsWith("last.jpeg")){
//        System.out.println("Helllo AGAIN!!!"+fileName);
					lastImage = file.getAbsolutePath();
				}
			}
			// firstImage = "/home/cavisson/work/OpenCvDemo/speedIndex/Page_0/video_clip_0_0_0_0_0_0_0_0_0_4_0_904_110776_100_0_0.jpeg";
			// lastImage = "/home/cavisson/work/OpenCvDemo/speedIndex/Page_0/video_clip_0_0_0_0_0_0_0_0_0_4_16_39808_564840_100_0_0_last.jpeg";
			srcFirst = Imgcodecs.imread(firstImage, 1);
			srcLast = Imgcodecs.imread(lastImage, 1);

			Imgproc.cvtColor(srcFirst, hsvFirst, Imgproc.COLOR_BGR2HSV);
			Imgproc.cvtColor(srcLast, hsvLast, Imgproc.COLOR_BGR2HSV);
			// Using 50 bins for hue and 60 for saturation
			int hBins = 50;
			int sBins = 60;
			int[] histSize = {hBins, sBins};

			//hue varies from 0 to 179, saturation from 0 to 255
			float[] ranges = {0, 180, 0, 256};

			//Use the 0-th and 1-st channels
			int[] channels = {0, 1};

			Mat  histFirst = new Mat(), histLast = new Mat();

			// Calculate the histograms for the HSV images

			List<Mat> hsvFirstList = Arrays.asList(hsvFirst);
			Imgproc.calcHist(hsvFirstList, new MatOfInt(channels), new Mat(), histFirst, new MatOfInt(histSize), new MatOfFloat(ranges), false);
			Core.normalize(histFirst, histFirst, 0, 1, Core.NORM_MINMAX);
			List<Mat> hsvTest2List = Arrays.asList(hsvLast);
			Imgproc.calcHist(hsvTest2List, new MatOfInt(channels), new Mat(), histLast, new MatOfInt(histSize), new MatOfFloat(ranges), false);
			Core.normalize(histLast, histLast, 0, 1, Core.NORM_MINMAX);


			double base = Imgproc.compareHist(histLast, histFirst, compare_method);

			for (i = 0; base > 0 && i < noOfSnapshots ; i++) { // i<number_of_snapshots
				Mat srcTest, hsvTest = new Mat();
				Mat histTest = new Mat();
				String testImage = fileArr[i].getAbsolutePath();
				//String testImage = "/home/cavisson/work/OpenCvDemo/speedIndex/Page_0/video_clip_0_0_0_0_0_0_0_0_0_4_12_2914_569392_100_0_0.jpeg";
				srcTest = Imgcodecs.imread(testImage, 1);
				Imgproc.cvtColor(srcTest, hsvTest, Imgproc.COLOR_BGR2HSV);
				List<Mat> hsvTestList = Arrays.asList(hsvTest);
				Imgproc.calcHist(hsvTestList, new MatOfInt(channels), new Mat(), histTest, new MatOfInt(histSize), new MatOfFloat(ranges), false);
				Core.normalize(histTest, histTest, 0, 1, Core.NORM_MINMAX);

				//Apply the histogram comparison methods
				double test = Imgproc.compareHist(histLast, histTest, compare_method);
				double factor = 100.00 / base;
				double distance = test * factor;
				if (distance > 100.00 && retryCount != 0) {
					rebaseFlag = 1;
					retryCount--;
					rebaseIndex++;
				}
				double prog = 100.00 - distance;
				int index = clipTimeIndex(path);//

				speedIndexVal[index] = prog;
			}
		}while(rebaseFlag>0 && retryCount>0);

		maxTime = clipTimeIndex(path)+1;
		for(i=1;i<maxTime-1;i++) {
			if (speedIndexVal[i] < 0.000001 || speedIndexVal[i] > 100.00) {
				speedIndexVal[i] = speedIndexVal[i-1];
			}
		}

		for(i = 0; i<maxTime; i++){
			intervalScore = (1.00 - (speedIndexVal[i]/100.00))*100.00;
			sumSpeedIndex += intervalScore;
			System.out.println("Sum Speed Index:" + sumSpeedIndex);

		}
		speedIndex = (int)Math.round(sumSpeedIndex);
		System.out.println("Speed Index:" + speedIndex);

		return speedIndex;
	}
	public static void main(String[] args) {
		System.out.println(calSpeedIndex("/Users/vibhoraggarwal/downloads/Page_0"));
	}
}

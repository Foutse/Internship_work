 Project Template
=======================
This is a scala version of the mean shift code https://github.com/beckgael/Mean-Shift-LSH

Thus the same parameters where maintained:
Parameters

k is the number of neighbours to look at in order to compute centroid.

nbseg is the number of segments on which the data vectors are projected during LSH. Its value should usually be larger than 20, depending on the data set.

nbblocs1 is a crucial parameter as larger values give faster but less accurate LSH approximate nearest neighbors, and as smaller values give slower but more accurate approximations.

nbblocs2 as larger values give faster but less accurate LSH approximate nearest neighbors, and as smaller values give slower but more accurate approximations.

cmin is the threshold under which clusters with fewer than cmin members are merged with the next nearest cluster.
normalisation is a flag if the data should be first normalized (X-Xmin)/(Xmax-Xmin) before clustering.

w is a uniformisation constant for LSH.

ratioToStop is the percentage of data that need to not have converged in order to considere to stop gradient ascent iterations, set to 1.0 to disable it.

yStarIter is the maximum number of iterations in the gradient ascent in the mean shift update.

epsilon1 is the threshold under which we considere a mod to have converged.

epsilon2 is the threshold under which two final mean shift iterates are considered to be in the same cluster.

epsilon3 is the threshold under which two final clusters are considered to be the same.

nbLabelIter is the number of iteration for the labelisation step, it determines the number of final models

Image analysis

To carry out image analysis, it was recommended to convert the usual color formats (e.g. RGB, CYMK) to the Luv* color space as the close values in the Luv*-space correspond more to visual perceptions of color proximity, as well adding the row and column indices (x,y). Each pixel is transformed to a 5-dimensional vector (x,y,L*, u*, v*) which is then input into the mean shift clustering.

  val sc = new SparkContext(conf)
  val meanShift = msLsh.MsLsh
  val data = sc.textFile("myData.csv")
  val parsedData = data.map(_.split(',').map(_.toDouble)).map(y => (Vectors.dense(y)))
                        .zipWithIndex
                        .map(_.swap)
  
   val model = meanShift.train(sc=2,
                               data, 
                               k=60, 
                               epsilon1=0.001, 
                               epsilon2=0.05, 
                               epsilon3=0.06, 
                               ratioToStop=0.01, 
                               yStarIter=10, 
                               cmin=0, 
                               normalisation=true, 
                               w=1, 
                               nbseg=100, 
                               nbblocs1=50, 
                               nbblocs2=50, 
                               nbLabelIter=1)
                          
  // Save result for an image as (ID, Vector, ClusterNumber)
  meanShift.saveImageAnalysis(models.head, "MyImageResultDirectory",1)

  // Save result as (ID, ClusterNumber)
  meanShift.savelabeling(models.head, "MyResultDirectory", 1)

  // Save centroids result as (NumCluster, cardinality, CentroidVector)
  meanShift.saveClusterInfo(models.head, "centroidDirectory")

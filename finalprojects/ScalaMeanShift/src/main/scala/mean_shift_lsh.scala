
// See LICENSE.txt for license details.
//package example
package msLsh
//import chisel3._
//import chisel3.util.{DeqIO, EnqIO, log2Ceil}
import scala.collection.mutable.{ArrayBuffer, ListBuffer, HashMap}
import scala.util.Random
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
//import msLsh.
//import Fcts.example
/**
 * Mean-Shift-LSH clustering 
 * This algorithm could be used to analyse complex multivariate multidimensional data.
 * It can also be apply in order to analyse image, to use this features it is recommanded to convert image from RGB space to L*u*v* space
 */


/**
 * The major class where MS-LSH algorithm and prediction fonction are implemented
 */
 class MsLsh private (private var k:Int, private var epsilon1:Double, private var epsilon2:Double, private var epsilon3:Double, private var ratioToStop:Double, private var yStarIter:Int, private var cmin:Int, private var normalisation:Boolean, private var w:Double, private var nbseg:Int, private var nbblocs1:Int, private var nbblocs2:Int, private var nbLabelIter:Int) extends Serializable {  

  def this() = this(50, 0.001, 0.05, 0.05, 0.05, 10, 0, true, 1.0, 100, 100, 50, 5)
  
  /**
   * Set normalisation boolean
   */
  def set_boolnorm(bool1:Boolean) : this.type = {
    this.normalisation = bool1
    this
  }
    
  /**
   * Set w
   */
  def set_w(newW:Double) : this.type = {
    this.w = newW
    this
  }
  
  /**
   * Set image analysis boolean
   */
  def set_nbseg(nbseg1:Int) : this.type = {
    this.nbseg = nbseg1
    this
  }
 /**
   * Set image analysis boolean
   */
  def set_nbblocs1(bloc1:Int) : this.type = {
    this.nbblocs1 = bloc1
    this
  }
    
  /**
   * Set image analysis boolean
   */
  def set_nbblocs2(bloc2:Int) : this.type = {
    this.nbblocs2 = bloc2
    this
  }
  
  /**
   * Set k value
   */
  def set_k(kval:Int) : this.type = {
    this.k = kval
    this
  }
  
  /**
   * Set threshold 1 for gradient ascent stop
   */
  def set_ratioToStop(ratioToStop_val:Double) : this.type = {
    this.ratioToStop = ratioToStop_val
    this
  }  

  /**
   * Set threshold 1 for gradient ascent stop
   */
  def set_epsilon1(epsilon1_val:Double) : this.type = {
    this.epsilon1 = epsilon1_val
    this
  }  
  
  /**
   * Set threshold 2 for labeling step
   */
  def set_epsilon2(epsilon2_val:Double) : this.type = {
    this.epsilon2 = epsilon2_val
    this
  }  

  /**
   * Set threshold 1 for labeling step
   */
  def set_epsilon3(epsilon3_val:Double) : this.type = {
    this.epsilon3 = epsilon3_val
    this
  }
  
  /**
   * Set iteration number for ascend gradient step
   */
  def set_yStarIter(yStarIter_val:Int) : this.type = {
    this.yStarIter = yStarIter_val
    this
  }
  
  /**
   * Set minimal cardinality for cluster
   */
  def set_cmin(cmin_val:Int) : this.type = {
    this.cmin = cmin_val
    this
  }  
    
  /**
   * Set number of time we makke labelizing step 
   */
  def set_nbLabelIter(nbLabelIter_val:Int) : this.type = {
    this.nbLabelIter = nbLabelIter_val
    this
  }  
  
  /**
   * Get k value
   */
  def get_k = this.k
    
  /**
   * Get nbblocs1 value
   */
  def get_nbblocs1 = this.nbblocs1
      
  /**
   * Get nbblocs2 value
   */
  def get_nbblocs2 = this.nbblocs2
  
  /**
   * Get threshold 1 value
   */
  def get_epsilon1 = this.epsilon1
    
  /**
   * Get threshold 2 value
   */
  def get_epsilon2 = this.epsilon2
      
  /**
   * Get threshold 3 value
   */
  def get_epsilon3 = this.epsilon3
  
  /**
   * Get number of iteration for gradient ascend
   */
  def get_yStarIter = this.yStarIter
  
  /**
   * Get minimal cardinality valueyStarIter
   */
  def get_cmin = this.cmin
      
  /**
   * Get number of labelizing iteration
   */
  def get_nbLabelIter = this.nbLabelIter
  
  /**
   * Get stoping ratio for gradient ascent
   */
  def get_ratioToStop = this.ratioToStop
  /**
   * Mean Shift LSH accomplish his clustering work
   */
  def run(dp:Int, data:Array[(Long, Array[Double])]) : ArrayBuffer[Mean_shift_lsh_model] = 
  {
    /**
    * Initialisation 
    */
    //data.cache
     val size = data.size
    //val size =  data.count.toInt
    val maxK = (size / nbblocs1).toInt -1 
   // val dp = sc.defaultParallelism

    if (size < cmin) throw new IllegalStateException("Exception : cmin > data size")
    if (maxK <= k) throw new IllegalStateException("Exception : You set a too high K value")
    /**
    * Dataset Normalisation
    */
   // val obs = new Fcts()
    val (normalizedOrNotRDD, maxArray, minArray) = if(normalisation) Fcts.scaleRdd(data) else (data, Array.empty[Double], Array.empty[Double])    
    val dim = normalizedOrNotRDD.head._2.size
    val maxMinArray = (maxArray, minArray) 
    /**
    * Gradient ascent / Research of Y* 
    */
    val b = Random.nextDouble * w
    var hashTab = Fcts.tabHash(nbseg, dim)
  
    val rdd_LSH = normalizedOrNotRDD.map{ case (id, vector) => (id, vector, vector, Fcts.hashfunc(vector, w, b, hashTab), false) }
   // data.unpersist(true)
    val lineageRDDs = new ArrayBuffer[Array[(Long, Array[Double], Array[Double], Double, Boolean)]]()
    lineageRDDs += rdd_LSH
   
    var stopIter = false
    var ind = 0
//we fix the number of iterations

 if( ratioToStop == 1.0 )
    {
      for( ind2 <- 0 until yStarIter )
      {
      // Increase convergence time
        //hashTab = Fcts.tabHash(nbseg, dim)
        val rdd_LSH_ord =  lineageRDDs(ind).sortBy({ case (_, _, _, hashValue, _) => hashValue}).grouped(k).map(it => {
          //val approxKNN = new ArrayBuffer[Double]
          val approxKNN = it.toArray
          approxKNN.map{
		case (id, originalVector, mod, hashV, stop) => {
            		val distKNNFromCurrentPoint = approxKNN.map{case (_, originalVector2, mod2, hashV2, _) => (originalVector2.map(_.toDouble), Fcts.sqdist(mod, originalVector2)) }.sortBy(_._2)
            val newMod = Fcts.computeCentroid(distKNNFromCurrentPoint.take(k), k)
            (id, originalVector, newMod, stop)
          }}.toIterator
        })
        //lineageRDDs += rdd_LSH_ord.map{ case (id, originalVector, mod, stop) => (id, originalVector, mod, Fcts.hashfunc(mod, w, b, hashTab), stop) }

      }
    }
    else
    { 
      while( ind < yStarIter && ! stopIter )
      {
        // Increase convergence time
        //hashTab = Fcts.tabHash(nbseg, dim)
        val rdd_LSH_ord =  lineageRDDs(ind).sortBy({ case (_, _, _, hashValue, _) => hashValue}).grouped(k).map(it => {
          val approxKNN = it.toArray
          approxKNN.map{ case (id, originalVector, mod, hashV, _) => {
            val distKNNFromCurrentPoint = approxKNN.map{ case (_, originalVector2, mod2, hashV2, _) => (originalVector2.map(_.toDouble), Fcts.sqdist(mod, originalVector2)) }.sortBy(_._2)
            val newMod = Fcts.computeCentroid(distKNNFromCurrentPoint.take(k), k)
            val stop = Fcts.sqdist(mod, newMod) <= epsilon1
            (id, originalVector, newMod, stop)
          }}//.toIterator
        }).toArray.flatten
	//rdd_LSH_ord._7
        lineageRDDs += rdd_LSH_ord.map{ case (id, originalVector, mod, stop) => (id, originalVector, mod, Fcts.hashfunc(mod, w, b, hashTab), stop) } //.cache
        val unconvergedPoints = lineageRDDs(ind + 1).filter{ case (_, _, _, _, stop) => stop == false }.size
        //lineageRDDs(ind).unpersist(false)
        stopIter = unconvergedPoints <= ratioToStop * size
        ind += 1
      }
    }
    val readyToLabelization = if( nbblocs2 == 1 ) lineageRDDs.last.map{ case (id, mod, originalVector, hashV, _) => (id, mod, originalVector)}
                              else lineageRDDs.last.sortBy{ case (_, _, _, hashValue, _) => hashValue}.map{ case (id, mod, originalVector, hashV, _) => (id, mod, originalVector)}
    
    //if( nbLabelIter > 1 ) readyToLabelization.cache

    val models = ArrayBuffer.empty[Mean_shift_lsh_model]
  /**
     * Labelization function which gathered mods together into clusters
     */
    val labelizing = () => 
    {
    /*  val clusterByLshBucket = readyToLabelization.grouped(dp).toList.zipWithIndex.map{ case (it,ind) => {
        val labeledData = ListBuffer.empty[(Int, (Long, Array[Double], Array[Double]))]
        val bucket = List(it).toBuffer
        var mod1 = bucket(Random.nextInt(bucket.size))
        var clusterID = (ind + 1)*10000
        while ( bucket.size != 0 )
        {
            val closestCentroids = bucket.filter{ case (_, mod, originalVector) => { Fcts.sqdist(mod, mod1) <= epsilon2 } }
            val closestCentroids2 = closestCentroids.map{ case (id, mod, originalVector) => (clusterID, (id, mod, originalVector))}
            labeledData ++= closestCentroids2
            bucket --= closestCentroids
            if( bucket.size != 0 ) mod1 = bucket(Random.nextInt(bucket.size))._2
            clusterID += 1
        }
        labeledData.toIterator
      }}*/

val clusterByLshBucket = readyToLabelization.grouped(dp).zipWithIndex.foldLeft(ListBuffer.empty[(Int, (Long, Array[Double], Array[Double]))]){ case (acc,(it,ind)) => {
        val labeledData = ListBuffer.empty[(Int, (Long, Array[Double], Array[Double]))]
        val bucket = it.toBuffer
        var mod1 = bucket(Random.nextInt(bucket.size))._2
        var clusterID = (ind + 1) * 10000
        while ( bucket.size != 0 )
        {
            val closestCentroids = bucket.filter{ case (_, mod, originalVector) => { Fcts.sqdist(mod, mod1) <= epsilon2 } }
            val closestCentroids2 = closestCentroids.map{ case (id, mod, originalVector) => (clusterID, (id, mod, originalVector))}
            labeledData ++= closestCentroids2
            bucket --= closestCentroids
            if( bucket.size != 0 ) mod1 = bucket(Random.nextInt(bucket.size))._2
            clusterID += 1
        }
        //labeledData.toIterator
        acc ++= labeledData
      }}
	




      /**
      * Gives Y* labels to original data
      */
	//val .... = Fcts.partition(clusterByLshBucket, dp)
      //val rdd_Ystar_labeled = clusterByLshBucket.groupBy{case (clusterID, (id, mod, originalVector)) => clusterID}
//	val rdd_Ystar_labeled = clusterByLshBucket.toArray.map{ case (clusterID, (id, mod, originalVector)) => (clusterID, (id, originalVector, mod)) }.partitionBy((new HashPartitioner(dp)))
	val rdd_Ystar_labeled = clusterByLshBucket.toArray.map{ case (clusterID, (id, mod, originalVector)) => (clusterID, (id, originalVector, mod)) }
		.groupBy{
			case key => {
				key match {
					case null => 0
					case _ => Fcts.nonNegativeMod(key.hashCode,dp)
				}
			}
		}
                                                  //(new HashPartitioner(dp))
        
      //val centroidMapOrig = rdd_Ystar_labeled.map{ case (clusterID, (_, originalVector, mod)) => (clusterID , originalVector.toArray) }.reduceByKeyLocally(_ + _)
 val centroidMapOrig = rdd_Ystar_labeled.map{ case (_, partition) => 
	val distinctClusterIds = partition.unzip._1.distinct
	distinctClusterIds.map{
		case(clusterId) => {
			val arrays = partition.filter{ case(cid,_) => cid == clusterId }
			val pureArrays = arrays.map{
				case (clusterID, (_, originalVector, mod)) => originalVector
			}
			val summed = pureArrays.reduceLeft[Array[Double]]{
				case(origVec1,origVec2) => Fcts.add(origVec1,origVec2)
			}
			(clusterId,summed)
		}
	}
  }.toArray.flatten
//val numElemByClust = rdd_Ystar_labeled.countByKey

      val numElemByCLust = rdd_Ystar_labeled.map{
	case(_,partition)=> {
		val distinctClusterIds = partition.unzip._1.distinct
		distinctClusterIds.map {
			case(clusterId) => {
				val size = partition.filter{ case(cid,_) => cid == clusterId}.length
				(clusterId,size)
			}
		}
	}
      }.flatten //.countByKey
                                                                         
	

      val centroids = ArrayBuffer.empty[(Int, Array[Double], Long)]

      // Form the array of clusters centroids
/*      for( (clusterID, cardinality) <- numElemByCLust )
        centroids += ( (clusterID, Fcts.dense(centroidMapOrig(clusterID).map(_ / cardinality)), cardinality) )
*/
val toto = centroidMapOrig.toMap
for( (clusterID, cardinality) <- numElemByCLust ) {
	val t = toto(clusterID).map(_ / cardinality)
	centroids += ( (clusterID, t, cardinality) )
}
       // centroids += ( (clusterID, Fcts.dense(centroidMapOrig.map(g => g.map{case (a,b)=>(a,b/cardinality)})), cardinality) )
      //centroids += ( (clusterID, Fcts.dense(centroidMapOrig.map{case(clusterID,a)=> (clusterID, a/ cardinality)}), cardinality) )
      /**
       * Fusion cluster with centroid < threshold
       */

      val newCentroids = ArrayBuffer.empty[(Int, Array[Double])]
      val numElemByCluster = HashMap.empty[Int, Long]
      val oldToNewLabelMap = HashMap.empty[Int, Int]
      var randomCentroidVector = centroids(Random.nextInt(centroids.size))._2
      var newClusterID = 0
      while ( ! centroids.isEmpty ) {
        val closestClusters = centroids.filter{ case (clusterID, vector, clusterCardinality) => { Fcts.sqdist(vector, randomCentroidVector) <= epsilon3 }}
        // We compute the mean of the cluster
        val gatheredCluster = closestClusters.map{ case (clusterID, vector, clusterCardinality) => (vector, clusterCardinality) }.reduce( (a, b) => (Fcts.add(a._1, b._1) , a._2 + b._2) )

        newCentroids += ( (newClusterID, gatheredCluster._1.map(_ / closestClusters.size)) )

        numElemByCluster += ( newClusterID -> gatheredCluster._2 )
        for( (clusterID, _, _) <- closestClusters ) {
          oldToNewLabelMap += ( clusterID -> newClusterID )
        }
        centroids --= closestClusters
        // We keep Y* whose distance is greather than threshold
        if( centroids.size != 0 ) randomCentroidVector = centroids(Random.nextInt(centroids.size))._2
        newClusterID += 1
      }

 /**
      * Fusion of cluster which cardinality is smaller than cmin 
      */
      val clusterIDsOfSmallerOne = numElemByCluster.filter{ case (clusterID, cardinality) => cardinality <= cmin }.keys.toBuffer
      val toGatherCentroids = newCentroids.zipWithIndex.map{ case ((clusterID, vector), id) => (id, clusterID, vector, numElemByCluster(clusterID), clusterID)}//.par
      val littleClusters = toGatherCentroids.filter{ case (_, _, _, cardinality, _) => cardinality <= cmin }.toBuffer




  while( ! clusterIDsOfSmallerOne.isEmpty )
      {
        val (idx, currentClusterID, origVector, sizeCurrent, _) = littleClusters(Random.nextInt(littleClusters.size)) 
        val sortedClosestCentroid = toGatherCentroids.map{ case (id, newClusterID, vector, cardinality, _) => (id, vector, Fcts.sqdist(vector, origVector), newClusterID, cardinality) }.sortBy{ case (_, _, dist, _, _) => dist }
        val (idx2, vector, _, closestClusterID, closestClusterSize) = sortedClosestCentroid.find(_._4 != currentClusterID).get
        var totSize = sizeCurrent + closestClusterSize
        val lookForNN = ArrayBuffer(vector, origVector)
        val oldClusterIDs = ArrayBuffer(currentClusterID, closestClusterID)
        val idxToReplace = ArrayBuffer(idx, idx2)
        while( totSize <= cmin )
        {
          val (idxK, vectorK, _, clusterIDK, cardinalityK) = lookForNN.map(v => toGatherCentroids.map{ case (id, newClusterID, vector, cardinality, _) => (id, vector, Fcts.sqdist(vector, origVector), newClusterID, cardinality) }.filter{ case (_, _, _, newClusterID, _) => ! oldClusterIDs.contains(newClusterID) }.sortBy{ case (_, _, dist, _, _) => dist }.head).sortBy{ case (_, _, dist, _, _) => dist }.head
          lookForNN += vectorK
          oldClusterIDs += clusterIDK
          idxToReplace += idxK
          totSize += cardinalityK
        }

        idxToReplace ++= toGatherCentroids.filter{ case (_, newClusterID, _, _, _) => newClusterID == closestClusterID }.map{ case (id, _, _, _, _) => id }

        for( idxR <- idxToReplace )
        {
          val (idR, _, vectorR, cardinalityR, originalClusterIDR) = toGatherCentroids(idxR)
          if( totSize > cmin)
          {
            clusterIDsOfSmallerOne -= originalClusterIDR
            littleClusters -= ( (idR, originalClusterIDR, vectorR, cardinalityR, originalClusterIDR) )
          }
          toGatherCentroids(idxR) = (idxR, closestClusterID, vectorR, totSize, originalClusterIDR)
        }
      }


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

      val newCentroidIDByOldOneMap = toGatherCentroids.map{ case (id, newClusterID, vector, cardinality, originalClusterID) => (originalClusterID, newClusterID) }.toMap

      val newCentroidIDByOldOneMapBC = oldToNewLabelMap.map{ case (k, v) => (k, newCentroidIDByOldOneMap(v)) }

/*val rdd_Ystar_labeled = clusterByLshBucket.toArray.map{ case (clusterID, (id, mod, originalVector)) => (clusterID, (id, originalVector, mod)) }
		.groupBy{
			case key => {
				key match {
					case null => 0
					case _ => Fcts.nonNegativeMod(key.hashCode,dp)
				}
			}
		}
    */

//val partitionedRDDF = rdd_Ystar_labeled.map{ case (clusterID, (id, originalVector, mod)) => (newCentroidIDByOldOneMapBC(clusterID), (id, originalVector)) }.partitionBy(new HashPartitioner(dp))
//rdd_Ystar_labeled._1
 val partitionedRDDF = rdd_Ystar_labeled.toArray.map{ case (_,p)=> p.map{case (clusterID,(id, originalVector, mod)) => (newCentroidIDByOldOneMapBC(clusterID), (id, originalVector))} }.flatten.groupBy{
			case key => {
				key match {
					case null => 0
					case _ => Fcts.nonNegativeMod(key.hashCode,dp)
				}
			}
		}    //.partitionBy(new HashPartitioner(dp))


    //      val partitionedRDDFforStats = partitionedRDDF.map{ case (clusterID, (id, originalVector)) => (clusterID, (originalVector.toArray)) }.cache
	//partitionedRDDF._1
      val partitionedRDDFforStats = partitionedRDDF.map{
                       case(hashCode,partition)=> partition.map{
                               case (clusterID, (id, originalVector)) => (clusterID, originalVector)
                                    } 
                      } //.cache
	//}


      //val clustersCardinalities = partitionedRDDFforStats.countByKey


 val clustersCardinalities = partitionedRDDFforStats.map{
	case(partition)=> {
		val distinctClusterIds = partition.unzip._1.distinct
		distinctClusterIds.map {
			case(clusterId) => {
				val size = partition.filter{ case(cid,_) => cid == clusterId}.length
				(clusterId,size)
			}
		}
      }
 }.flatten.toArray.toMap




     // val centroidF = partitionedRDDFforStats.reduceByKey(_ + _)
      //                    .map{ case (clusterID, reducedVectors) => (clusterID, Vectors.dense(reducedVectors.map(_ / clustersCardinalities(clusterID)))) }

val centroidF = partitionedRDDFforStats.map{ 
                          case (partition)=>{         
	val distinctClusterIds = partition.unzip._1.distinct
	distinctClusterIds.map{
		case(clusterId) => {
			val arrays = partition.filter{ case(cid,_) => cid == clusterId }
			val pureArrays = arrays.map{
				case (clusterID, originalVector) => originalVector
			}
			val summed = pureArrays.reduceLeft[Array[Double]]{
				case(origVec1,origVec2) => Fcts.add(origVec1,origVec2)
			}
			(clusterId,summed)
		}
	}
  }}.toArray.flatten.map{
	 case (clusterID, reducedVectors) => {
		val cardinality = clustersCardinalities(clusterID)
		(clusterID, reducedVectors.map(_ / cardinality))
	}
  }




      
      val centroidMap = if( normalisation ) Fcts.descaleRDDcentroid(centroidF, maxMinArray).toMap else centroidF.toMap
      val partitionedRDDF1=partitionedRDDF.map{ case (_,partition)=>partition}.toArray.flatten
      val msmodel = new Mean_shift_lsh_model(centroidMap, clustersCardinalities, partitionedRDDF1, maxMinArray)
      //rdd_Ystar_labeled.unpersist(true)
      //partitionedRDDFforStats.unpersist(true)
      msmodel 
    }

    for( ind <- 0 until nbLabelIter)
      models += labelizing()    

    //readyToLabelization.unpersist(false)
    models
  } 
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

object MsLsh {

  /**
   * Trains a MS-LSH model using the given set of parameters.
   *
   * @param sc : SparkContext`
   * @param data : an RDD[(String,Vector)] where String is the ID and Vector the rest of data
   * @param k : number of neighbours to look at during gradient ascent
   * @param epsilon1 : threshold under which we stop iteration in gradient ascent
   * @param epsilon2 : threshold under which we give the same label to two points
   * @param epsilon3 : threshold under which we give the same label to two close clusters
   * @param ratioToStop : % of data that have NOT converged in order to stop iteration in gradient ascent
   * @param yStarIter : Number of iteration for modes search
   * @param cmin : threshold under which we fusion little cluster with the nearest cluster
   * @param normalisation : Normalise the dataset (it is recommand to have same magnitude order beetween features)
   * @param w : regularisation term, default = 1
   * @param nbseg : number of segment on which we project vectors ( make sure it is big enought )
   * @param nbblocs1 : number of buckets used to compute modes
   * @param nbblocs2 : number of buckets used to fusion clusters
   * @param nbLabelIter : number of iteration for the labelisation step, it determines the number of final models
   *
   */

  def train(sc:Int, data:Array[(Long,Array[Double])], k:Int, epsilon1:Double, epsilon2:Double, epsilon3:Double, ratioToStop:Double, yStarIter:Int, cmin:Int, normalisation:Boolean, w:Double, nbseg:Int, nbblocs1:Int, nbblocs2:Int, nbLabelIter:Int) : ArrayBuffer[Mean_shift_lsh_model] =
      new MsLsh(k, epsilon1, epsilon2, epsilon3, ratioToStop, yStarIter, cmin, normalisation, w, nbseg, nbblocs1, nbblocs2, nbLabelIter).run(sc, data)




  /**
   * Restore RDD original value
   */
  val descaleRDD = (toDescaleRDD:Array[(Int, (Long, Array[Double]))], maxMinArray:(Array[Double], Array[Double])) => 
  {
    val vecttest = toDescaleRDD.head._2._2
    val size1 = vecttest.size
    val maxArray = maxMinArray._1
    val minArray = maxMinArray._2
    toDescaleRDD.map{ case (clusterID, (id, vector)) => {
      var tabcoord = new Array[Double](size1)
      for( ind0 <- 0 until size1 ) {
        val coordXi = vector(ind0) * (maxArray(ind0) - minArray(ind0)) + minArray(ind0)
        tabcoord(ind0) = coordXi
      }
      (clusterID, id, tabcoord)          
    } }
  }

  /**
   * Get result for image analysis
   * Results look's like RDD.[ID,Centroïd_Vector,cluster_Number]
   */
  def imageAnalysis(msmodel:Mean_shift_lsh_model) : Array[(Long, Array[Double], Int)] = 
    descaleRDD(msmodel.labelizedRDD, msmodel.maxMinArray).map{ case (clusterID, id, _) => (id, msmodel.clustersCenter(clusterID), clusterID) }

 def getTimeAsFormattedStr() = (new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")).format(new Date(System.currentTimeMillis))

 def saveLabelingWithTime(labels: Array[(Long, Array[Double], Int)], path: String="/home/kybe/Results/") =	{
		val labelsAsStr = labels.mkString("\n")
		val pathWithTime = path + getTimeAsFormattedStr()
		val fw = new FileWriter(pathWithTime, true)
		fw.write(labelsAsStr)
		fw.close
	}


 def saveLabelingWithTime2(labels: Array[(Long,Int)], path: String="/home/kybe/Results/") =	{
		val labelsAsStr = labels.mkString("\n")
		val pathWithTime = path + getTimeAsFormattedStr()
		val fw = new FileWriter(pathWithTime, true)
		fw.write(labelsAsStr)
		fw.close
	}

  /**
   * Save result for an image analysis
   * Results look's like RDD[ID,Centroïd_Vector,cluster_Number]
   */
  def saveImageAnalysis(msmodel:Mean_shift_lsh_model, path:String, numpart:Int=1) : Unit ={
   val data = descaleRDD(msmodel.labelizedRDD, msmodel.maxMinArray).map{ case (clusterID, id, vector) => (id, msmodel.clustersCenter(clusterID), clusterID) }.sortBy(_._1)
saveLabelingWithTime(data,path)

 //.saveLabelingWithTime(path)
}
//val data= saveImageAnalysis(msmodel:Mean_shift_lsh_model, path:String, numpart:Int)


  /**
   * Get an RDD[ID,cluster_Number]
   */
  def getlabeling(msmodel:Mean_shift_lsh_model) : Array[(Long, Int)] = msmodel.labelizedRDD.map{ case (clusterID, (id, _)) => (id, clusterID) }

  /**
   * Save labeling as (ID,cluster_Number)
   */
  def savelabeling(msmodel:Mean_shift_lsh_model, path:String, numpart:Int=1) ={
   val data2= msmodel.labelizedRDD.map{ case (clusterID, (id, vector)) => (id, clusterID) }.sortBy(_._1)
saveLabelingWithTime2(data2,path)
}
  /**
   * Save clusters's label, cardinality and centroid
   */
  def saveClusterInfo(msmodel:Mean_shift_lsh_model, path:String) : Unit = {
    val centroidsWithID = msmodel.clustersCenter.toArray
    val cardClust = msmodel.clustersCardinalities 
    val strToWrite = centroidsWithID.map{ case (clusterID, centroid) => (clusterID ,cardClust(clusterID), centroid) }.sortBy(_._1).mkString("\n")
    val fw = new FileWriter(path, true)
    fw.write(strToWrite)
    fw.close
  }

  /*
   * Prediction function which tell in which cluster a vector should belongs to
   */
  def prediction(v:Array[Double], mapCentroid:Map[Int,Array[Double]]) : Int = mapCentroid.map{ case (clusterID, centroid) => (clusterID, Fcts.sqdist(v, centroid)) }.toArray.sortBy(_._2).head._1
}










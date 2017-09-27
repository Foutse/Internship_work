package example
import scala.io.Source
/**
 *An implementation of the k-nearest neighbours classification algo
 *@param k The number of neighbours to use for prediction.
 *@param dataX Matrix of input examples.
 *@param dataY Corresponding output to classes
 *@param distanceFn Functionused to compute 'near-ness.
 */
object Main {
  def main(args: Array[String]): Unit = {

  class NearestNeighbours(k: Int,
                        dataX: Array[Array[Double]],
                        dataY: Seq[String],
                        distanceFn: (Array[Double],Array[Double])=>Double){
/**
  *Predict the output class corresponding to a given input example
  *@param x input example
  *@return Predicted class
  */
     def predict(X:Array[Double]):String ={
        val distances = dataX.map(y=> distanceFn(y,X))
        val topKClasses = distances.zipWithIndex.sortBy(_._1).take(k).map{case (dist, idx)=>dataY(idx)}
        topKClasses.groupBy(identity).mapValues(_.size).maxBy(_._2)._1
     }
  }

  def line2Data(line: String): (List[Double],String)={
     val elts=line.split(",")
     val y= elts.last
     val x= elts.dropRight(1).map(_.toDouble).toList
    (x,y)
  }
  val data = Source.fromFile("ionosphere.data.txt").getLines().map(x=>line2Data(x)).toList
  val outputs =data.map(_._2).toSeq
  val inputs =data.map(_._1).toArray
  val euclideanDist=(v1:Array[Double], v2:Array[Double]) =>v1.zip(v2).map(x=>math.pow((x._1-x._2),2)).sum

  val traininputs=inputs.take(300)  //use the first 300 data points of our data sets
  val trainoutputs=outputs.take(300)
  val myNN=new NearestNeighbours(k=4,dataX=traininputs.map(x=>x.toArray),dataY=trainoutputs,euclideanDist)

  var correct=0
  (300 to 350).foreach{ exampleId =>
     val pred=myNN.predict(inputs(exampleId).toArray)
     val target=outputs(exampleId)
     if (pred==target) correct +=1
  }
println("The accuracy is\n" + correct.toDouble /(300 to 350).length)
}

}






 

	

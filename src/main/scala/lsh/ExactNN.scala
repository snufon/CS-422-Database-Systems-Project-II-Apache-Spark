package lsh

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext


class ExactNN(sqlContext: SQLContext, data: RDD[(String, List[String])], threshold: Double) extends Construction with Serializable {


  override def eval(rdd: RDD[(String, List[String])]): RDD[(String, Set[String])] = {
    /*
    * This method performs a near-neighbor computation for the data points in rdd against the data points in data.
    * Near-neighbors are defined as the points with a Jaccard similarity that exceeds the threshold
    * data: data points in (movie_name, [keyword_list]) format to compare against
    * rdd: data points in (movie_name, [keyword_list]) format that represent the queries
    * threshold: the similarity threshold that defines near-neighbors
    * return near-neighbors in (movie_name, [nn_movie_names]) as an RDD[(String, Set[String])]
    * */

    rdd.cartesian(data) // ((rddmovie,rddtags),(datamovie,datatags))
      .filter { case ((rddMovie, rddTags), (dataMovie, dataTags)) =>
        val numEqual = rddTags.intersect(dataTags).length
        val numTotal = rddTags.union(dataTags).length
        val jaccard = numEqual.toDouble / numTotal
        jaccard > threshold
      }
      .map { case ((rddMovie, rddTags), (dataMovie, dataTags)) => (rddMovie, dataMovie) }
      .groupBy(x => x._1)
      .map{ case (movie,list) => (movie,list.map(_._2).toSet)}
  }
}


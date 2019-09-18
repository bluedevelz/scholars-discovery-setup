package duke

import java.io.Closeable

import org.apache.jena.tdb.TDBFactory
import org.apache.jena.query.Dataset
import org.apache.jena.tdb.TDB

import org.apache.jena.query.QueryExecution
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.ResultSetFormatter

import org.apache.jena.query.ReadWrite
import org.apache.jena.query.ResultSet
import org.apache.jena.util.FileManager

import org.apache.jena.riot.RDFDataMgr

import org.apache.logging.log4j.LogManager

class TDBConnector(val base: String) : Closeable {
    var ds: Dataset

    companion object Factory {
        private val logger = LogManager.getLogger()
    }

    init {
        logger.debug("establishing TDBConnection")
        val directory = "../data-imported/$base"
        ds = TDBFactory.createDataset(directory)
    }

    fun query(sparql: String): QueryExecution {
        logger.debug("making query")
        return QueryExecutionFactory.create(sparql, ds)
    }

    fun importDukeData() {
        logger.debug("importing duke data")
        ds.begin(ReadWrite.WRITE)
        val model = ds.getNamedModel("duke")
        RDFDataMgr.read(model, "../content.trig")
        model.close()
        ds.commit()
        ds.end()
    }

    fun importFloridaData() {
        logger.debug("importing florida data")
        ds.begin(ReadWrite.WRITE)
        val model = ds.getNamedModel("florida")
        val fileManager = FileManager()
        fileManager.addLocatorZip("../sample-data/uf/uf01.ttl.zip")
        fileManager.addLocatorZip("../sample-data/uf/uf02.ttl.zip")
        fileManager.addLocatorZip("../sample-data/uf/uf03.ttl.zip")

        fileManager.readModel(model, "uf01.ttl")
        fileManager.readModel(model, "uf02.ttl")
        fileManager.readModel(model, "uf03.ttl")

        model.close()
        ds.commit()
        ds.end()
    }

    fun importOpenVivoData() {
        logger.debug("importing openvivo data")
        ds.begin(ReadWrite.WRITE)
        // NOTE: I tried ds.getDefaultModel() it never seemed to work
        val model = ds.getNamedModel("openvivo")
        val fileManager = FileManager()
        fileManager.addLocatorZip("../sample-data/openvivo/openvivo.ttl.zip")
        fileManager.readModel(model, "openvivo.ttl")
        model.close()
        ds.commit()
        ds.end()
    }

    override fun close() {
        logger.debug("closing TDBConnection")
        ds.close()
    }
}

fun importData(dataset: String) {
    val connector = TDBConnector(dataset)
    connector.use { c ->
        when (dataset) {
            "openvivo" -> c.importOpenVivoData()
            "florida" -> c.importFloridaData()
            "duke" -> c.importDukeData()
            else -> println("dataset must be one of (openvivo|duke|florida)")
        }
    }
}

/*
this doesn't work (with florida data):

  <${uri}> core:relatedBy ?authorship .
  ?authorship vitro:mostSpecificType ?authorshipType .
  ?authorship core:relates ?publication .
  ?publication a obo:IAO_0000030 .
  ?publication rdfs:label ?label .

 */
fun findPublicationsSparql(uri: String): String {
    val pubSparql = """
      PREFIX vivo: <http://vivoweb.org/ontology/core#>
      PREFIX obo: <http://purl.obolibrary.org/obo/>
      PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

      SELECT * WHERE {
        <$uri> vivo:authorInAuthorship ?authorship .
        ?authorship vivo:linkedInformationResource ?publication .
        ?publication rdfs:label ?title .
      }
    """
    return pubSparql
}
// http://xmlns.com/foaf/0.1/Person
fun listPeople(dataset: String) {
    println("trying to run query: $dataset")
    val connector = TDBConnector(dataset)

    println("dataset empty?=${connector.ds.isEmpty}")

    // florida: 177449
    /*
    val sparql = """
        PREFIX foaf:     <http://xmlns.com/foaf/0.1/>

        SELECT (count(distinct(?x)) as ?personcount)
        WHERE {
            ?x a foaf:Person .
        }
    """
    */

    val sparql = """
        PREFIX foaf:     <http://xmlns.com/foaf/0.1/>
        PREFIX vivo: <http://vivoweb.org/ontology/core#>
        PREFIX bibo: <http://purl.org/ontology/bibo/>
        PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>

        SELECT ?x
        WHERE {
            ?x a foaf:Person .
        }
        LIMIT 1000
    """

    connector.use { c ->
        val qe = c.query(sparql)
        qe.context.set(TDB.symUnionDefaultGraph, true)
        val results: ResultSet = qe.execSelect()
        for (r in results) {
            println(r)
            val uri = r.get("x")
            val pubSparql = findPublicationsSparql(uri.toString())

            val qe2 = c.query(pubSparql)
            // need to set this everytime?
            qe2.context.set(TDB.symUnionDefaultGraph, true)
            val results2: ResultSet = qe2.execSelect()
            println("uri=${uri}")
            for (p in results2) {
                //println(p)
                val pub = p.get("title")
                println("____pub->${pub}")
            }
        }
        //ResultSetFormatter.out(results)
    }
}

// gradle run --args='foo --bar'
fun main(args: Array<String>) {
    // args[0] = '(openvivo|florida|duke)'
    // args[1] = '--import'
    if (args.isEmpty()) {
        println("Need to at least specify which dataset (openvivo|florida|duke)")
        println("usage ./gradlew run --args='(openvivo|florida|duke)'")
        println("to import:")
        println("usage ./gradlew run --args='(openvivo|florida|duke) --import'")
    } else if (args.size == 1) {
        listPeople(args[0])
    } else if (args.size == 2 && args[1] == "--import") {
        importData(args[0])
    }
}
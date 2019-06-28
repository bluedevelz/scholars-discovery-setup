package duke

import java.io.Closeable

import org.apache.jena.tdb.TDBFactory
import org.apache.jena.query.Dataset

import org.apache.jena.query.QueryExecution
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.ResultSetFormatter

import org.apache.jena.query.ReadWrite
import org.apache.jena.query.ResultSet
import org.apache.jena.util.FileManager

import org.apache.jena.rdf.model.Model
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
        //val model: Model = ds.getDefaultModel()
        val model = ds.getNamedModel("duke")
        RDFDataMgr.read(model, "../content.trig")
        model.close()
        ds.commit()
        ds.end()
    }

    fun importFloridaData() {
        logger.debug("importing florida data")
        ds.begin(ReadWrite.WRITE)
        //val model: Model = ds.getDefaultModel()
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
        val fileManager: FileManager = FileManager()
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
fun importOpenVivoData() {
    val connector = TDBConnector("openvivo")
    connector.use { c ->
        c.importOpenVivoData()
    }
}

fun importDukeData() {
    val connector = TDBConnector("duke")
    connector.use { c ->
        c.importDukeData()
    }
}

fun importFloridaData() {
    val connector = TDBConnector("florida")
    connector.use { c ->
        c.importFloridaData()
    }
}
*/

// http://xmlns.com/foaf/0.1/Person
fun listPeople(dataset: String) {
    println("trying to run query")
    val connector = TDBConnector(dataset)

    val sparql = """
        PREFIX foaf:     <http://xmlns.com/foaf/0.1/>
        SELECT * WHERE {
            ?x a foaf:Person .
            ?x ?p ?y .
        } ORDER BY ?x LIMIT 100
    """

    connector.use { c ->
        val qe = c.query(sparql)
        val results: ResultSet = qe.execSelect()
        ResultSetFormatter.out(results)
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
    } else if (args.size == 2 && args[1] == "--import"){
        importData(args[0])
    }
}

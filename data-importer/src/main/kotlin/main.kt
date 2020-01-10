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
import org.apache.jena.riot.Lang
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

    /*
    fun describe(sparql: String): QueryExecution {
        logger.debug("running describe")
        return QueryExecutionFactory.create(sparql, ds)
    }
    */

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

fun findPublicationsSparql2(uri: String): String {
    val pubSparql = """
      PREFIX vivo: <http://vivoweb.org/ontology/core#>
      PREFIX obo: <http://purl.obolibrary.org/obo/>
      PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>

      SELECT * WHERE {
        <${uri}> vivo:relatedBy ?authorship .
        ?authorship vitro:mostSpecificType ?authorshipType .
        ?authorship vivo:relates ?publication .
        ?publication a obo:IAO_0000030 .
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
        #LIMIT 1000
    """

    connector.use { c ->
        val qe = c.query(sparql)
        qe.context.set(TDB.symUnionDefaultGraph, true)
        val results: ResultSet = qe.execSelect()
        for (r in results) {
            println(r)
            val uri = r.get("x")
            val pubSparql = when (dataset) {
                "florida" -> findPublicationsSparql(uri.toString())
                else -> findPublicationsSparql2(uri.toString())
            }
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

fun findOrgSparql(uri: String): String {
    val orgSparql = """
      PREFIX vivo: <http://vivoweb.org/ontology/core#>
      PREFIX foaf:     <http://xmlns.com/foaf/0.1/>
      PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      PREFIX obo: <http://purl.obolibrary.org/obo/>
      
      SELECT * WHERE {
         #<${uri}> vivo:relatedBy ?organization .
         <${uri}> vivo:relates ?organization .
         ?organization a foaf:Organization .
         ?organization rdfs:label ?label .
      }
    """
    return orgSparql
}

fun describeSparql(uri: String): String {
    val sparql = """
        DESCRIBE <${uri}>
    """
    return sparql
}

/*
uri=http://openvivo.org/a/n10411
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix ns2:   <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> .
@prefix ns1:   <http://vivoweb.org/ontology/core#> .
@prefix ns4:   <http://www.w3.org/2006/vcard/ns#> .
@prefix ns3:   <http://purl.org/ontology/bibo/> .
@prefix ns6:   <http://aims.fao.org/aos/geopolitical.owl#> .
@prefix ns5:   <http://purl.obolibrary.org/obo/> .
@prefix ns8:   <http://xmlns.com/foaf/0.1/> .
@prefix ns7:   <http://vitro.mannlib.cornell.edu/ns/vitro/public#> .
@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix ns9:   <http://vivo.mydomain.edu/ns#> .
@prefix xml:   <http://www.w3.org/XML/1998/namespace> .
@prefix ns11:  <http://www.w3.org/2002/07/owl#> .
@prefix ns10:  <http://purl.org/dc/terms/> .

<http://openvivo.org/a/n10411>
        a                     ns1:Position , ns5:BFO_0000001 , ns1:Relationship , ns1:LibrarianPosition , ns11:Thing , ns5:BFO_0000002 , ns5:BFO_0000020 ;
        rdfs:label            "Head of Digital Collections & Preservation Systems" ;
        ns2:mostSpecificType  ns1:LibrarianPosition ;
        ns1:dateTimeInterval  <http://openvivo.org/a/n31076> ;
        ns1:relates           <http://openvivo.org/a/grid.21729.3f> , <http://openvivo.org/a/orcid0000-0003-2588-3084> , <http://openvivo.org/a/grid.36425.36> .

 */
fun listRelationships(dataset: String) {
    println("trying to run query: $dataset")
    val connector = TDBConnector(dataset)

    println("dataset empty?=${connector.ds.isEmpty}")

    val sparql = """
        PREFIX vivo: <http://vivoweb.org/ontology/core#>

        SELECT ?x
        WHERE {
            ?x a vivo:Relationship .
        }
        #LIMIT 1000
    """

    connector.use { c ->
        val qe = c.query(sparql)
        qe.context.set(TDB.symUnionDefaultGraph, true)
        val results: ResultSet = qe.execSelect()
        for (r in results) {
            //println(r)
            val uri = r.get("x")
            println("uri=${uri}")


            val orgSparql = findOrgSparql(uri.toString())
            val qe2 = c.query(orgSparql)
            // need to set this everytime?
            qe2.context.set(TDB.symUnionDefaultGraph, true)
            val results2: ResultSet = qe2.execSelect()
            for (p in results2) {
                println(p)
                //val pub = p.get("title")
                //println("____pub->${pub}")
            }

            /*
            val qe3 = c.query(describeSparql(uri.toString()))
            qe3.context.set(TDB.symUnionDefaultGraph, true)
            val model = qe3.execDescribe()
            RDFDataMgr.write(System.out, model, Lang.TURTLE)

             */
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
        //    <{{uri}}> vivo:relatedBy ?organization .
        //    ?organization a foaf:Organization .
        //listPeople(args[0])
        listRelationships(args[0])
    } else if (args.size == 2 && args[1] == "--import") {
        importData(args[0])
    }
}
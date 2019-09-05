package org.dice_research.opal.launuts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import io.github.galbiston.geosparql_jena.implementation.datatype.WKTDatatype;
import io.github.galbiston.geosparql_jena.implementation.vocabulary.Geo;

public class ModelBuilder {

	private Model model = ModelFactory.createDefaultModel();
	private Map<String, Resource> nuts3map = new HashMap<String, Resource>();

	private final static boolean ADD_TYPE = false;
	private final static boolean ADD_NARROWER = false;

	ModelBuilder() {
		model.setNsPrefix("dct", Vocabularies.NS_DCTERMS);
		model.setNsPrefix("lau", Vocabularies.NS_LAU);
		model.setNsPrefix("nuts", Vocabularies.NS_NUTS);
		model.setNsPrefix("skos", Vocabularies.NS_SKOS);
		model.setNsPrefix("geo", Vocabularies.NS_GEO);
		model.setNsPrefix("xsd", Vocabularies.NS_XSD);
		model.setNsPrefix("ogc", Vocabularies.NS_OGC);

		// Additional prefixes to reduce model size
		model.setNsPrefix("laude", Vocabularies.NS_LAU_DE);
		model.setNsPrefix("nutscode", Vocabularies.NS_NUTS_CODE);
	}

	public ModelBuilder addNuts(Collection<NutsContainer> nutsCollection) {
		for (NutsContainer container : nutsCollection) {

			Resource nuts = getModel().createResource(container.getUri());

			if (ADD_TYPE) {
				getModel().add(nuts, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.SKOS.Concept);
			}

			if (container.parent != null) {
				// Not available for root
				getModel().add(nuts, org.apache.jena.vocabulary.SKOS.broader,
						model.getResource(container.parent.getUri()));
				if (ADD_NARROWER) {
					getModel().add(model.getResource(container.parent.getUri()),
							org.apache.jena.vocabulary.SKOS.narrower, nuts);
				}
			}

			getModel().add(nuts, org.apache.jena.vocabulary.SKOS.notation,
					getModel().createLiteral(container.notation));

			for (String prefLabel : container.prefLabel) {
				getModel().add(nuts, org.apache.jena.vocabulary.SKOS.prefLabel, getModel().createLiteral(prefLabel));
			}

			nuts3map.put(container.notation, nuts);
		}
		return this;
	}

	public ModelBuilder addGeoData(Map<String, DbpediaPlaceContainer> dbpediaIndex, Map<String, String> nutsToDbpedia,
			Map<String, String> lauToDbpedia) {

		for (Entry<String, String> nuts2dbp : nutsToDbpedia.entrySet()) {
			Resource res = ResourceFactory.createResource(nuts2dbp.getKey());
			if (getModel().containsResource(res) && dbpediaIndex.containsKey(nuts2dbp.getValue())) {
				Literal wkt = ResourceFactory.createTypedLiteral("POINT(" + dbpediaIndex.get(nuts2dbp.getValue()).lat
						+ " " + dbpediaIndex.get(nuts2dbp.getValue()).lon + ")", WKTDatatype.INSTANCE);
				getModel().addLiteral(res, Geo.HAS_GEOMETRY_PROP, wkt);
			}
		}

		for (Entry<String, String> lau2dbp : lauToDbpedia.entrySet()) {
			Resource res = ResourceFactory.createResource(lau2dbp.getKey());
			if (getModel().containsResource(res) && dbpediaIndex.containsKey(lau2dbp.getValue())) {
				Literal wkt = ResourceFactory.createTypedLiteral("POINT(" + dbpediaIndex.get(lau2dbp.getValue()).lat
						+ " " + dbpediaIndex.get(lau2dbp.getValue()).lon + ")", WKTDatatype.INSTANCE);
				getModel().addLiteral(res, Geo.HAS_GEOMETRY_PROP, wkt);
			}
		}

		return this;
	}

	public ModelBuilder addLau(List<LauContainer> lauList) {
		for (LauContainer container : lauList) {
			Resource lau = getModel().createResource(container.getUri());
			if (nuts3map.containsKey(container.nuts3code)) {

				if (ADD_TYPE) {
					getModel().add(lau, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.SKOS.Concept);
				}

				getModel().add(lau, org.apache.jena.vocabulary.SKOS.broader, nuts3map.get(container.nuts3code));
				if (ADD_NARROWER) {
					getModel().add(nuts3map.get(container.nuts3code), org.apache.jena.vocabulary.SKOS.narrower, lau);
				}

				getModel().add(lau, org.apache.jena.vocabulary.SKOS.notation,
						getModel().createLiteral(container.lauCode));

				getModel().add(lau, org.apache.jena.vocabulary.SKOS.prefLabel,
						getModel().createLiteral(container.getSimpleName()));
				if (!container.getSimpleName().equals(container.lauNameLatin)) {
					getModel().add(lau, org.apache.jena.vocabulary.SKOS.altLabel,
							getModel().createLiteral(container.lauNameLatin));
				}
			} else {
				System.err.println("Unknown NUTS3 code: " + container.nuts3code + " for " + container.lauCode);
				continue;
			}
		}
		return this;
	}

	public Model getModel() {
		return model;
	}

	public String getTurtleComment() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("# LAU and NUTS data for Germany\n");
		stringBuilder.append("# \n");
		stringBuilder.append("# Local Administrative Units (LAU)\n");
		stringBuilder.append("# Nomenclature of Territorial Units for Statistics (NUTS)\n");
		stringBuilder.append("# https://ec.europa.eu/eurostat/web/nuts/overview\n");
		stringBuilder.append("# \n");
		stringBuilder.append("# Generator software: \n");
		stringBuilder.append("# Data Science Group (DICE) at Paderborn University\n");
		stringBuilder.append("# Open Data Portal Germany (OPAL), Adrian Wilke\n");
		stringBuilder.append("# https://github.com/projekt-opal/LauNuts\n");
		stringBuilder.append("# \n");
		stringBuilder.append("# Data:\n");
		stringBuilder.append("# https://hobbitdata.informatik.uni-leipzig.de/OPAL/\n");
		stringBuilder.append("# \n");

		return stringBuilder.toString();
	}

	public ModelBuilder writeModel(File outputDirectory) throws IOException {
		File file = new File(outputDirectory, "launuts.ttl");
		file.getParentFile().mkdirs();
		FileOutputStream outputStream = new FileOutputStream(file);
		outputStream.write(getTurtleComment().getBytes());
		RDFDataMgr.write(outputStream, model, Lang.TURTLE);
		return this;
	}

}
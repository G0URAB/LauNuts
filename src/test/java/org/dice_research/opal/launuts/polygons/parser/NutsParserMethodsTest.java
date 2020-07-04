package org.dice_research.opal.launuts.polygons.parser;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class NutsParserMethodsTest {

	@Test
	public void getInnerRings() throws IOException, ParseException {
		// Read JSON array to pass in the method
		NutsParser nutParser = new NutsParser();
		nutParser.geojsonReader = new FileReader("src/test/resources/nuts_synthetic_one/synthetic_nuts3.geojson");
		JSONObject rootObject = (JSONObject) nutParser.jsonParser.parse(nutParser.geojsonReader);
		JSONArray feature = (JSONArray) rootObject.get("features");
		JSONObject firstObject = (JSONObject) feature.get(0);
		JSONObject jsonGeometry = (JSONObject) firstObject.get("geometry");
		JSONArray coordinates = (JSONArray) jsonGeometry.get("coordinates");
		JSONArray inner_rings = nutParser.getInnerRings(coordinates);
		
		// Read JSON array to compare with the result
		nutParser.geojsonReader = new FileReader("src/test/resources/nuts_synthetic_one/synthetic_nuts4.geojson");
		JSONObject rootObject1 = (JSONObject) nutParser.jsonParser.parse(nutParser.geojsonReader);
		JSONArray feature1 = (JSONArray) rootObject1.get("features");
		JSONObject firstObject1 = (JSONObject) feature1.get(0);
		JSONObject jsonGeometry1 = (JSONObject) firstObject1.get("geometry");
		JSONArray coordinatesExpected = (JSONArray) jsonGeometry1.get("coordinates");
		Assert.assertEquals("The result array returns inner-rings", coordinatesExpected, inner_rings);
	}

	@Test
	public void getCoordinatesLatLongFormat() throws IOException, ParseException {
		// Read JSON array to pass in the method
		NutsParser nutsParser = new NutsParser();
		nutsParser.geojsonReader = new FileReader("src/test/resources/nuts_synthetic_one/synthetic_nuts1.geojson");
		JSONObject rootObject = (JSONObject) nutsParser.jsonParser.parse(nutsParser.geojsonReader);
		JSONArray feature = (JSONArray) rootObject.get("features");
		JSONObject firstObject = (JSONObject) feature.get(0);
		JSONObject jsonGeometry = (JSONObject) firstObject.get("geometry");
		JSONArray coordinates = (JSONArray) jsonGeometry.get("coordinates");
		JSONArray latLong = NutsParser.getCoordinatesLatLongFormat(coordinates);

		// Read JSON array to compare with the result
		nutsParser.geojsonReader = new FileReader("src/test/resources/nuts_synthetic_one/synthetic_nuts2.geojson");
		JSONObject rootObject1 = (JSONObject) nutsParser.jsonParser.parse(nutsParser.geojsonReader);
		JSONArray feature1 = (JSONArray) rootObject1.get("features");
		JSONObject firstObject1 = (JSONObject) feature1.get(0);
		JSONObject jsonGeometry1 = (JSONObject) firstObject1.get("geometry");
		JSONArray coordinatesExpected = (JSONArray) jsonGeometry1.get("coordinates");
		Assert.assertEquals("The result array will be equal to coordinates ", coordinatesExpected, latLong);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void areValidPolygons() {
		double[][] arrays = { { 50.13351, 8.81796 }, { 50.07466, 8.8237 }, { 50.06241, 8.71783 }, { 50.13673, 8.78065 },
				{ 50.13351, 8.81796 } };
		JSONObject coordinates = new JSONObject();
		coordinates.put("coordinates", arrays);
		JSONArray first_ring = new JSONArray();
		JSONArray coordinates_of_polygon = new JSONArray();
		first_ring.add(coordinates);
		coordinates_of_polygon.add(first_ring);
		String geometry_type = "polygon_type";
		NutsParser obj = new NutsParser();
		Boolean valid_polygon = obj.areValidPolygons(coordinates_of_polygon, geometry_type);
		Assert.assertEquals("It must return true", true, valid_polygon);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void areInValidPolygons() {
		double[][] arrays = { { 50.13351, 8.81796 }, { 50.07466, 8.8237 }, { 50.06241, 8.71783 },
				{ 50.13673, 8.78065 } };
		JSONObject coordinates = new JSONObject();
		coordinates.put("coordinates", arrays);
		JSONArray first_ring = new JSONArray();
		JSONArray coordinates_of_polygon = new JSONArray();
		first_ring.add(coordinates);
		coordinates_of_polygon.add(first_ring);
		String geometry_type = "multipolygon";
		NutsParser obj = new NutsParser();
		Boolean invalid_polygon = obj.areValidPolygons(coordinates_of_polygon, geometry_type);
		Assert.assertEquals("It must return false", false, invalid_polygon);

	}
}

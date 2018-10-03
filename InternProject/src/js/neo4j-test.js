var neo4j=require('neo4j-driver').v1;
var turf = require('@turf/turf');
var rewind=require('geojson-rewind');

const driver = neo4j.driver("bolt://localhost", neo4j.auth.basic("neo4j", "oayoub"));
const session = driver.session();
function isRightHandRule(geom) {
    // Hack to deepcopy geom and avoid mutation
    geomstring = JSON.stringify(geom);
    geomcopy = JSON.parse(geomstring);

    // Does the original geometry match the clockwise-wound geometry
    return geomstring === JSON.stringify(rewind(geomcopy, true));
}
const resultPromise = session.run(
    'MATCH (n:Town) RETURN n'
);

resultPromise.then(result => {
    session.close();

    const singleRecord = result.records[0];
    const node = singleRecord.get(0);
    var test=node.properties.geoshape;
    console.log(isRightHandRule(rewind(test,true)));
    var polygon=rewind(node.properties.geoshape,false);
    if(polygon===test){
        console.log(polygon); 
    }
    
    // on application exit:
    driver.close();
});

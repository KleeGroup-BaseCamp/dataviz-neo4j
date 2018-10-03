d3.sankey = function() {
  var sankey = {},
      nodeWidth = 24,
      nodePadding = 8,
      size = [1, 1],
      nodes = [],
      links = [],
    // cycle features
    cycleLaneNarrowWidth = 4,
    cycleLaneDistFromFwdPaths = -10,  // the distance above the paths to start showing 'cycle lanes'
    cycleDistFromNode = 30,      // linear path distance before arcing from node
      cycleControlPointDist = 30,  // controls the significance of the cycle's arc
    cycleSmallWidthBuffer = 10  // distance between 'cycle lanes'
    ;

  sankey.nodeWidth = function(_) {
    if (!arguments.length) return nodeWidth;
    nodeWidth = +_;
    return sankey;
  };

  sankey.nodePadding = function(_) {
    if (!arguments.length) return nodePadding;
    nodePadding = +_;
    return sankey;
  };

  // cycle related attributes
  sankey.cycleLaneNarrowWidth = function(_) {
    if (!arguments.length) return cycleLaneNarrowWidth;
    cycleLaneNarrowWidth = +_;
    return sankey;
  }

  sankey.cycleSmallWidthBuffer = function(_) {
    if (!arguments.length) return cycleSmallWidthBuffer;
    cycleSmallWidthBuffer = +_;
    return sankey;
  }

  sankey.cycleLaneDistFromFwdPaths = function(_) {
    if (!arguments.length) return cycleLaneDistFromFwdPaths;
    cycleLaneDistFromFwdPaths = +_;
    return sankey;
  }

  sankey.cycleDistFromNode = function(_) {
    if (!arguments.length) return cycleDistFromNode;
    cycleDistFromNode = +_;
    return sankey;
  }

  sankey.cycleControlPointDist = function(_) {
    if (!arguments.length) return cycleControlPointDist;
    cycleControlPointDist = +_;
    return sankey;
  }

  sankey.nodes = function(_) {
    if (!arguments.length) return nodes;
    nodes = _;
    return sankey;
  };

  sankey.links = function(_) {
    if (!arguments.length) return links;
    links = _;
    return sankey;
  };

  sankey.size = function(_) {
    if (!arguments.length) return size;
    size = _;
    return sankey;
  };

  sankey.layout = function(iterations) {

    computeNodeLinks();
    computeNodeValues();
    markCycles();

    var branchsAndForks = getBranchsAndForks(),
        branchs = branchsAndForks.branchs,
        forks = branchsAndForks.forks;

    computeNodeBreadths(branchs, forks);
    computeNodeDepths(iterations, branchs, forks);
    computeLinkDepths();
    return sankey;
  };

  sankey.relayout = function() {
    computeLinkDepths();
    return sankey;
  };

  sankey.link = function() {
    var curvature = .3;

    function link(d) {

      var xs, xt, ys, yt ; 

      nodes.forEach(function(node){
        if(d.source.localeCompare(node.name) === 0 ){
          xs = node.x;
          ys = node.y /*+ d.sy + d.dy / 2*/;
        }
        else if(d.target.localeCompare(node.name) === 0 ){
          xt = node.x;
          yt = node.y /*+ d.sy + d.dy / 2*/;
        }
      }); 

    if( d.causesCycle ) {
      // cycle node; reaches backward

      /*
      The path will look like this, where
      s=source, t=target, ?q=quadratic focus point
     (wq)-> /-----n-----\
            |w          |
            |           e
            \-t         |
                     s--/ <-(eq)
      */
      // Enclosed shape using curves n' stuff
      var smallWidth = cycleLaneNarrowWidth,

      s_x = xs//d.source.x + d.source.dx,
      s_y = ys//d.source.y + d.sy + d.dy,
      t_x = xt//d.target.x,
      t_y = yt - 2*d.dy//d.target.y,
    se_x = s_x + cycleDistFromNode,
    se_y = s_y,
    ne_x = se_x,
    ne_y = (/*cycleLaneDistFromFwdPaths*/s_y - (/*d.cycleIndex **/ 1.5*(smallWidth + cycleSmallWidthBuffer) )),  // above regular paths, in it's own 'cycle lane', with a buffer around it
    nw_x = (t_x - cycleDistFromNode),
    nw_y = ne_y,
    sw_x = nw_x,
    sw_y = (t_y + d.ty + d.dy);

      // start the path on the outer path boundary
    return "M" + s_x + "," + s_y
    + "L" + se_x + "," + se_y
    + "C" + (se_x + cycleControlPointDist/8) + "," + se_y 
    + " " + (ne_x + cycleControlPointDist/8) + "," + ne_y 
    + " " + ne_x + "," + ne_y
    + "H" + nw_x
    + "C" + (nw_x - cycleControlPointDist/8) + "," + nw_y 
    + " " + (sw_x - cycleControlPointDist/8) + "," + sw_y 
    + " " + sw_x + "," + sw_y
    + "H" + t_x
    //moving to inner path boundary
 /*   + "V" + ( t_y + d.ty )
    + "H" + sw_x
    + "C" + (sw_x - (cycleControlPointDist/2) + smallWidth) + "," + t_y + " " +
            (nw_x - (cycleControlPointDist/2) + smallWidth) + "," + (nw_y + smallWidth) + " " +
        nw_x + "," + (nw_y + smallWidth)
    + "H" + (ne_x - smallWidth)
    + "C" + (ne_x + (cycleControlPointDist/2) - smallWidth) + "," + (ne_y + smallWidth) + " " +
            (se_x + (cycleControlPointDist/2) - smallWidth) + "," + (se_y - d.dy) + " " +
        se_x + "," + (se_y - d.dy)
    + "L" + s_x + "," + (s_y - d.dy)*/;

    } else {
      // regular forward node
      var x0 = xs, //d.source.x + d.source.dx,
          x1 = xt,//d.target.x,
          xi = d3.interpolateNumber(x0, x1),
          x2 = xi(curvature),
          x3 = xi(1 - curvature),
          y0 = ys,//d.source.y + d.sy + d.dy / 2,
          y1 = yt, // d.target.y + d.ty + d.dy / 2;
          y2 = 0 ,
          y3 = 0, 
          x4 = 0,
          x5 = 0,
          x6 = 0,
          y4 = 0,
          y6 = 0;

      if(Math.abs(xs - xt) < 1.5*min_interval()){

        return "M" + x0 + "," + y0
             + "C" + x2 + "," + y0
             + " " + x3 + "," + y1
             + " " + x1 + "," + y1;
      }
      else{

        x2 = Math.min(xs, xt) + cycleControlPointDist/20//Math.abs(xt-xs)/10;
        x3 = Math.min(xs, xt) + cycleControlPointDist/16//Math.abs(xt-xs)/8;
        x4 = Math.max(xs, xt) - cycleControlPointDist/16//Math.abs(xt-xs)/8;
        x5 = Math.max(xs, xt) - cycleControlPointDist/20//Math.abs(xt-xs)/10;
        x6 = Math.abs(xt+xs)/2;
        y2 = Math.min(y0, y1) + Math.abs(y1-y0)/10;
        y3 = Math.min(y0, y1) + Math.abs(y1-y0)/8;
        y4 = y3;
        y5 = Math.max(y0, y1) - Math.abs(y1-y0)/10;
 
        return  "M" + x0 + "," + y0
             + "C" + x2 + "," + y2
             + " " + x3 + "," + y3
             + " " + x6 + "," + y4
             + "C" + x4 + "," + y4
             + " " + x5 + "," + y5
             + " " + x1 + "," + y1;
      }
    }
  }

  function min_interval(){
    var x_min = +Infinity,    
        x_max = -Infinity,
        x_positions = new Set();

    nodes.forEach(function(node){
      x_positions.add(node.x);
      x_min = node.x < x_min ? node.x : x_min ;
      x_max = node.x > x_max ? node.x : x_max ;        
    })
    return (x_max - x_min) / x_positions.size ;
  }

    function writePath(d ){

    }

    link.curvature = function(_) {
      if (!arguments.length) return curvature;
      curvature = +_;
      return link;
    };

    return link;
  };

  // Populate the sourceLinks and targetLinks for each node.
  // Also, if the source and target are not objects, assume they are indices.
  function computeNodeLinks() {
    nodes.forEach(function(node) {
      node.sourceLinks = [];
      node.targetLinks = [];
    links.forEach(function(link) {

      if(link.source.localeCompare(node.name) === 0 ){
        node.sourceLinks.push(link);
      }
      else if (link.target.localeCompare(node.name) === 0 ){
        node.targetLinks.push(link);
      };
    });
   });
  }

  // Compute the value (size) of each node by summing the associated links.
  function computeNodeValues() {
    nodes.forEach(function(node) {
      node.value = 60;
    });
  }

  // Iteratively assign the breadth (x-position) for each node.
  // Nodes are assigned the maximum breadth of incoming neighbors plus one;
  // nodes with no incoming links are assigned breadth zero, while
  // nodes with no outgoing links are assigned the maximum breadth.
  function computeNodeBreadths(branchs, forks) {

    var remainingNodes = nodes,
        nextNodes,

        x = 0, 
        visitedNodes = new Set();

    while (remainingNodes.length) {
      nextNodes = [];
      remainingNodes.forEach(function(node) {
        node.x = x;
        node.dx = nodeWidth;
        node.sourceLinks.forEach(function(link) {
      if( !link.causesCycle ) {
        nodes.forEach(function(node2){
          if(node2.name.localeCompare(link.target) === 0 /*&& !visitedNodes.has(node2)*/){
            nextNodes.push(node2);
            visitedNodes.add(node2);
          }
        })
            
      }
        });
      });
      remainingNodes = nextNodes;
      ++x;
    }

    // Bring branches closer to the fork they are linked with
    var allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs).concat(branchs.loops);
    allBranchs.forEach(function(branch){
      if(branch.endNode.targetLinks.length === 0){
        for(var i = 1 ; i < branch.nodes.length ; i++){
          branch.nodes[i].x = branch.nodes[i-1].x - 1 ;
        }
      }
      else if(branch.beginningNode.targetLinks.length === 0 && branch.nodes[branch.nodes.length -1].x - branch.nodes[branch.nodes.length -2].x > 1){
        for(var i = 1 ; i < branch.nodes.length ; i++){
          branch.nodes[branch.nodes.length - i -1].x = branch.nodes[branch.nodes.length - i].x - 1 ;
        }
      }
    })
    scaleNodeBreadths((size[0] - nodeWidth) / (x - 1));
  }

  function getSource(targetLink){

    var source; 
    nodes.forEach(function(node){
      if(node.name.localeCompare(targetLink.source) === 0){
        source = node;
        return source;
      }

    }); 
    return source ; 
  }

  function getTarget(sourceLink){

    var target; 
    nodes.forEach(function(node){
      if(node.name.localeCompare(sourceLink.target) === 0){
        target = node;
        return target;
      }

    }); 
    return target ; 
  }

/** We use the data as branches and forks
  * A branch has a beginning node and an end node, and all the nodes between these two nodes
  * All the branches are linked with forks, which have a forkNode and all its related branches
  **/
function getBranchsAndForks(){

  var forkNodes = getForksNodes();
   var branchs = {"leftBranchs": [], "rightBranchs": [], "loops": []};


  // Ajout des branches gauches
  getLeftBranchs(branchs, forkNodes);

  // Ajout des branches droites
  getRightBranchs(branchs, forkNodes);

  // Suppression des doublons
  var toRemove = [];
  branchs.rightBranchs.forEach(function(rightBranch){
    branchs.leftBranchs.forEach(function(leftBranch){
      if(areReversedBranchs(rightBranch, leftBranch)){
        toRemove.push(leftBranch);
      }
    });
  });
  toRemove.forEach(function(branch){
    if(branchs.leftBranchs.indexOf(branch) > -1){
      branchs.leftBranchs.splice(branchs.leftBranchs.indexOf(branch), 1);
    }
  })


  // Création des boucles
  if(branchs.leftBranchs.length !== 0 || branchs.rightBranchs.length !== 0){
    findLoops(branchs, forkNodes); 
  }

 var forks =  getForks(branchs.rightBranchs.concat(branchs.leftBranchs).concat(branchs.loops), forkNodes.leftNodes.concat(forkNodes.rightNodes));

  return {"branchs": branchs, "forks": forks}

}

function areReversedBranchs(branchA, branchB){

  var reversed = true;

  if(branchA.nodes.length !== branchB.nodes.length){
    reversed = false ; 
  }
  else{
    for(var i = 0 ; i < branchA.nodes.length ; i++){
      if(branchA.nodes[i].name.localeCompare(branchB.nodes[branchB.nodes.length - 1 - i].name) !== 0 ){
        reversed = false;
      }
    }
  }

  return reversed;

}

function getLeftBranchs(branchs, forkNodes){
    forkNodes.leftNodes.forEach(function(leftNode){
    
    for(var i = 0 ; i < leftNode.targetLinks.length ; i ++){

      var branch = {"nodes": [], "beginningNode": null, "endNode": null};
      branch.nodes.push(leftNode);
      var node = getSource(leftNode.targetLinks[i]); 
      while(node.sourceLinks.length === 1 && node.targetLinks.length === 1){
        branch.nodes.push(node);
        node = getSource(node.targetLinks[0]);
      }

      branch.nodes.push(node);
      branch.beginningNode = leftNode;
      branch.endNode = node; 
      branchs.leftBranchs.push(branch); 
    }

    if(leftNode.sourceLinks.length > 0){
      var branch = {"nodes": [], "beginningNode": null, "endNode": null};
      branch.nodes.push(leftNode);

      var node = getTarget(leftNode.sourceLinks[0]); 
      while(node.sourceLinks.length === 1 && node.targetLinks.length === 1){
        branch.nodes.push(node);
        node = getTarget(node.sourceLinks[0]);
      }

      branch.nodes.push(node);
      branch.beginningNode = leftNode;
      branch.endNode = node; 
      branchs.rightBranchs.push(branch); 
    }
  }); 
}

function getRightBranchs(branchs, forkNodes){
  forkNodes.rightNodes.forEach(function(rightNode){
   
    for(var i = 0 ; i < rightNode.sourceLinks.length ; i ++){
      var branch = {"nodes": [], "beginningNode": null, "endNode": null};
      branch.nodes.push(rightNode);

      var node = getTarget(rightNode.sourceLinks[i]); 
      while(node.sourceLinks.length === 1 && node.targetLinks.length === 1){
        branch.nodes.push(node);
        node = getTarget(node.sourceLinks[0]);
      }
      branch.nodes.push(node);
      branch.beginningNode = rightNode;
      branch.endNode = node; 

      if(branchs.rightBranchs.indexOf(branch) < 0){
        branchs.rightBranchs.push(branch); 
      }
    }
    if(rightNode.targetLinks.length > 0){
      var branch = {"nodes": [], "beginningNode": null, "endNode": null};
      branch.nodes.push(rightNode);

      for(var i = 0 ; i < rightNode.targetLinks.length ; i++){
        var node = getSource(rightNode.targetLinks[i]); 
        while(node.sourceLinks.length === 1 && node.targetLinks.length === 1){
          branch.nodes.push(node);
          node = getSource(node.targetLinks[0]);
        }
        branch.nodes.push(node);
        branch.beginningNode = rightNode;
        branch.endNode = node; 
        if(branchs.leftBranchs.indexOf(branch) < 0){
          branchs.leftBranchs.push(branch); 
        }
      }
    }
  }); 
}

function getForksNodes(){

  var forkNodes = { "leftNodes" : [], "rightNodes" : []};
  var endNodes = [];

  nodes.forEach(function(node){

    if(node.sourceLinks.length > 1){
      forkNodes.rightNodes.push(node);
    }
    if(node.targetLinks.length > 1){
      forkNodes.leftNodes.push(node);
    }
    if(node.targetLinks.length === 0 && node.sourceLinks.length === 1){
      endNodes.push(node);
    }
  }); 

  endNodes.forEach(function(node0){
    var node = getTarget(node0.sourceLinks[0]); 
    do{
      node = getTarget(node.sourceLinks[0]);
    }while(node.sourceLinks.length === 1 && node.targetLinks.length === 1)
    if(node.sourceLinks.length === 0){
      forkNodes.leftNodes.push(node0);
    }
  });


  return forkNodes;

}

function getForks(allBranchs, allForkNodes){

  var forks = [];
  allBranchs.forEach(function(branch){
    allForkNodes.forEach(function(node){
     if(branch.beginningNode === node || branch.endNode === node){
        var newFork = true;
        forks.forEach(function(fork){
          if(fork.forkNode === node){
            fork.branchs.push(branch);
            newFork = false;
          }
        })
        if(newFork){
          forks.push({"branchs": [branch], "forkNode": node});
        }
      }
    })
  })

  return forks;

}

function fromBranchsToGraph(branchs, forkNodes){

  // On crée toutes les branches possibles, dans un sens ou dans l'autre
  var rightBranchs = branchs.rightBranchs,
      leftBranchs = branchs.leftBranchs,
      reversedRightBranchs = [],
      reversedLeftBranchs = [];

  rightBranchs.forEach(function(branch){
    reversedRightBranchs.push(reverseBranch(branch));
  });
  leftBranchs.forEach(function(branch){
    reversedLeftBranchs.push(reverseBranch(branch));
  });

  var allBranchs = rightBranchs.concat(leftBranchs).concat(reversedRightBranchs).concat(reversedLeftBranchs);
  var forks = getForks(allBranchs, forkNodes.leftNodes.concat(forkNodes.rightNodes));

  var nodes = [],
      neighbors = [];

  forks.forEach(function(fork){
    nodes.push(fork.forkNode);
    var currentNeighbors = [];
    fork.branchs.forEach(function(branch){
      var nodeToAdd = branch.beginningNode === fork.forkNode ? branch.endNode : branch.beginningNode;
      if(currentNeighbors.indexOf(nodeToAdd) < 0){
        currentNeighbors.push(branch.beginningNode === fork.forkNode ? branch.endNode : branch.beginningNode);
      }
    })
    neighbors.push(currentNeighbors);
  })

  var nodes2 = [], 
      neighbors2 = [];
  
  for(var i = 0 ; i < nodes.length ; i++){
    nodes2.push(nodes[i]);
    neighbors2.push(neighbors[i]);
  } 
  
  for(var i = 0 ; i < neighbors.length ; i++){
    for(var j = 0 ; j < neighbors[i].length ; j++){
      if(nodes2.indexOf(neighbors[i][j]) < 0){ // noeud non enregistré précedemment
        nodes2.push(neighbors[i][j]);
        neighbors2.push([nodes[i]]);
      }
      else if(nodes.indexOf(neighbors[i][j]) < 0 // nouveau voisin d'un noeud existant
        && (neighbors2[nodes2.indexOf(neighbors[i][j])]).indexOf(nodes[i]) < 0){

        neighbors2[nodes2.indexOf(neighbors[i][j])].push(nodes[i]);
      }
    }
  }

  return {"nodes": nodes2, "neighbors" : neighbors2};
}


function getPathsOfNodes(graph, source, target){

  var nodes = graph.nodes,
      neighbors = graph.neighbors,
      allPaths = [],
      path = new Array(nodes.length);
      visited = [];

  for(var i = 0 ; i < nodes.length ; i++){
    visited.push(false);
  }

  getPathsRec(source, target, visited, path, 0, nodes, neighbors, allPaths);
  return allPaths;

}

function getPathsRec(source, target, visited, path, pathIndex, nodes, neighbors, allPaths){
  var pathCopy = [];
  for(var i = 0 ; i < path.length ; i++){
    pathCopy.push(path[i]);
  }

  var currentIndex ;// = nodes.indexOf(source) ; 
  for(var i = 0 ; i < nodes.length ; i++){
    if(nodes[i].name.localeCompare(source.name) === 0){
      currentIndex = i ;
    }
  }

  pathCopy[pathIndex] = source ;
  pathIndex++;

  if(source === target){

    allPaths.push(pathCopy);
  }
  else{ 
    visited[currentIndex] = true;
    for(var i = 0 ; i < neighbors[currentIndex].length ; i++){
      if(!visited[nodes.indexOf(neighbors[currentIndex][i])]){
        getPathsRec(neighbors[currentIndex][i], target, visited, pathCopy, pathIndex, nodes, neighbors, allPaths);
      }
    }
  }

}

function getPathsOfBranchs(paths, branchs){

  allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs);

  var pathsOfBranchs = [];

  paths.forEach(function(path){
    var pathOfBranchs = [];
    for(var i = 0 ; i < path.length -1 ; i++){
      if(path[i] != undefined){
        allBranchs.forEach(function(branch){
          if(((branch.beginningNode == path[i] && branch.endNode == path[i+1])
            || branch.endNode == path[i] && branch.beginningNode == path[i+1])
            && !branchInArray(pathOfBranchs, branch) && !branchInArray(pathOfBranchs, reverseBranch(branch))){
            pathOfBranchs.push(branch);
          }
        });
      }
    }
    pathsOfBranchs.push(pathOfBranchs);        
  })

  return pathsOfBranchs ; 

}

function getPaths(source, target, graph, branchs){

  return getPathsOfBranchs(getPathsOfNodes(graph, source, target), branchs); 
}

function findLoops(branchs, forkNodes){

  var allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs),
      graph = fromBranchsToGraph(branchs, forkNodes),
      pathsOfLoops = [];

  allBranchs.forEach(function(branch){
    // On détermine tous les itinéraires possibles du noeud de début au noeud de fin
    var pathsFromBeginningToEnd = getPaths(branch.beginningNode, branch.endNode, graph, branchs);
    if(pathsFromBeginningToEnd.length > 1){ // On a une boucle 
      var loopIndex,
          loopLength = Infinity ;

      // On détermine la boucle comme étant la plus courte branche de longueur 1 parmis les itinéraires possibles
      for(var i = 0 ; i < pathsFromBeginningToEnd.length ; i++){
        if(pathsFromBeginningToEnd[i].length === 1 && pathsFromBeginningToEnd[i][0].nodes.length < loopLength){
          loopIndex = i;
          loopLength = pathsFromBeginningToEnd[i][0].nodes.length;
        }
      }

      branchs.loops.push(pathsFromBeginningToEnd[loopIndex][0]);
      pathsOfLoops.push(pathsFromBeginningToEnd);
    }

  })

  // Dans branchs.loops, on peut avoir plusieurs segments appartenant à la même boucle
  // Dans ce cas, on garde le plus long/court
  sortLoops(branchs, pathsOfLoops);

}

// Paths est un tableau de chemins, l'indice i du tableau étant les chemins loops de la branche i
function sortLoops(branchs, paths){

  var partOfSameLoops = new Set();
  for(var i = 0 ; i < branchs.loops.length ; i++){

    paths[i].forEach(function(path){

  //    if(path.length > 1){
        var allPartsOfSameLoop = true;
        for(var j = 0 ; j < path.length ; j++){
          if(branchs.loops.indexOf(path[j]) === -1){
            allPartsOfSameLoop = false;
          }
  //      }
        if(allPartsOfSameLoop){
          var partOfSameLoop = new Set();
          partOfSameLoop.add(branchs.loops[i]);
          for(var j = 0 ; j < path.length ; j++){
            partOfSameLoop.add(path[j]);
            if(!inSet(partOfSameLoops, partOfSameLoop)){
              partOfSameLoops.add(partOfSameLoop);
            }
          }
        }        
      }

    })
  }

  var mainLoops = [];
  var minorLoops = [];

  partOfSameLoops.forEach(function(partOfSameLoop){
    var longestBranch,
        longestLength = 0;
    partOfSameLoop.forEach(function(branch){
      if(branch.nodes.length > longestLength){
        longestBranch = branch;
        longestLength = branch.nodes.length;
      }
    })
    mainLoops.push(longestBranch);
    partOfSameLoop.forEach(function(branch){
      if(mainLoops.indexOf(branch) < 0){
        minorLoops.push(branch);
      }
    })
  })

  minorLoops.forEach(function(loopToRemove){
    while(branchs.loops.indexOf(loopToRemove) > -1){ // Normalement, on n'a qu'une occurence, mais mieux vaut être prudent
      branchs.loops.splice(branchs.loops.indexOf(loopToRemove), 1);
    }
  })
  mainLoops.forEach(function(loopToRemove){
    while(branchs.rightBranchs.indexOf(loopToRemove) > -1){
      branchs.rightBranchs.splice(branchs.rightBranchs.indexOf(loopToRemove), 1);
    }
    while(branchs.leftBranchs.indexOf(loopToRemove) > -1){
      branchs.leftBranchs.splice(branchs.leftBranchs.indexOf(loopToRemove), 1);
    }

  })

  function inSet(set, loops){
    var inSet = false;

    set.forEach(function(registeredLoops){
      var sameLoops = true;
      loops.forEach(function(loop){
        if(!registeredLoops.has(loop)){
          sameLoops = false;
        }
      })
      if(sameLoops){
        inSet = true;
      }
    })  
    return inSet ; 
  }

}


function reverseBranch(branch){

  var reversedNodes = [];
  for(var i = 0 ; i < branch.nodes.length ; i++){
    reversedNodes.push(branch.nodes[branch.nodes.length - i - 1]);
  }
  return {"nodes": reversedNodes, "beginningNode": branch.endNode, "endNode": branch.beginningNode};
}

function equalNodes(nodesA, nodesB){
   var equal;
  if(nodesA.length !== nodesB.length){
    equal = false;
  }
  else{
    equal = true;
    for(var i = 0 ; i < nodesA.length ; i++){
      if(nodesA[i].name.localeCompare(nodesB[i].name) !== 0){
        equal = false;
      }
    }
  }
  return equal;
}

function equalBranchs(branchA, branchB){

  return equalNodes(branchA.nodes, branchB.nodes) 
          && branchA.beginningNode.name.localeCompare(branchB.beginningNode.name) === 0 
          && branchA.endNode.name.localeCompare(branchB.endNode.name) === 0
}

function branchInArray(branchs, branch0){
  var isInArray = false;
  branchs.forEach(function(branch){
    if(branch.beginningNode.name.localeCompare(branch0.beginningNode.name) === 0 
      && branch.endNode.name.localeCompare(branch0.endNode.name) === 0
      && equalNodes(branch0, branch)){
      isInArray = true;
    }
  })
  return isInArray; 
}


  function moveSourcesRight() {
    nodes.forEach(function(node) {
      if (!node.targetLinks.length) {
        node.x = d3.min(node.sourceLinks, function(d) { return d.target.x; }) - 1;
      }
    });
  }

  function moveSinksRight(x) {
    nodes.forEach(function(node) {
      if (!node.sourceLinks.length) {
        node.x = x - 1;
      }
    });
  }

  function scaleNodeBreadths(kx) {
    nodes.forEach(function(node) {
      node.x *= kx;
    });
  }

  function computeNodeDepths(iterations, branchs, forks) {

    var allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs).concat(branchs.loops),
        nodesByBreadth = d3.nest()
        .key(function(d) { return d.x; })
        .sortKeys(d3.ascending)
        .entries(nodes)
        .map(function(d) { return d.values; });

    initializeNodeDepth();

    if(branchs.leftBranchs.length !== 0 || branchs.rightBranchs.length !== 0 || branchs.loops.length !== 0){
      initializeNodeY(branchs, forks);
    var untangledH = true, 
        untangledV = true ;
    var i = 0 ;
    while((untangledH || untangledV) && i < 25){
      i++;
      replaceNodes(branchs);
      alignBranchs2(branchs);
      replaceNodes(branchs);
      resolveCollisions(branchs);
      replaceNodes(branchs);
      untangledH = untangleBranchsHorizontally(forks);
      replaceNodes(branchs);
      resolveCollisions(branchs);
      replaceNodes(branchs);
      untangledV = untangleBranchsVertically(branchs);
    }
    replaceNodes(branchs);
//    alignBranchs2(branchs);
    resolveCollisions(branchs);
    replaceNodes(branchs);

    var y_max = -Infinity,
        y_min = +Infinity;

    nodes.forEach(function(node){
      y_max = node.y > y_max ? node.y : y_max;
      y_min = node.y < y_min ? node.y : y_min;
    })
    if(y_max > -2*y_min){
      pushToTop();
    }
  }

  function initializeNodeDepth() {
    nodesByBreadth.forEach(function(nodes) {
      nodes.forEach(function(node, i) {
        node.y = i;
        node.dy = node.value ;
      });
    });

    links.forEach(function(link) {
      link.dy = link.value = 10; 
    });
  }

  function relaxLeftToRight(alpha) {
    nodesByBreadth.forEach(function(nodes, breadth) {
      nodes.forEach(function(node) {
        if (node.targetLinks.length) {
          var y = d3.sum(node.targetLinks, weightedSource) / d3.sum(node.targetLinks, value);
          node.y += (y - center(node)) * alpha;
        }
      });
    });

    function weightedSource(link) {
      return center(link.source) * link.value;
    }
  }

  function relaxRightToLeft(alpha) {
    nodesByBreadth.slice().reverse().forEach(function(nodes) {
      nodes.forEach(function(node) {
        if (node.sourceLinks.length) {
          var y = d3.sum(node.sourceLinks, weightedTarget) / d3.sum(node.sourceLinks, value);
          node.y += (y - center(node)) * alpha;
        }
      });
    });

    function weightedTarget(link) {
      return center(link.target) * link.value;
    }
  }

  function initializeNodeY(branchs, forks) {

    var mainPath = findMainPath(branchs, fromBranchsToGraph(branchs, getForksNodes()))
        allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs).concat(branchs.loops);

    var x_min = Infinity;
    var node_min;
    var node_begin;

    nodes.forEach(function(node){
      if(node.x < x_min){
        x_min = node.x;
        node_min = node;
      }
    })

    allBranchs.forEach(function(branch){
      if(branch.beginningNode ===  node_min || branch.endNode === node_min){
        node_begin = branch.beginningNode === node_min ? branch.endNode : branch.beginningNode;
      }
    })

    allBranchs.sort(function(bA, bB){ 
      var result = Math.min(bA.beginningNode.x, bA.endNode.x) - Math.min(bB.beginningNode.x, bB.endNode.x);
      if(result === 0){
        result = - (Math.max(bA.beginningNode.x, bA.endNode.x) - Math.max(bB.beginningNode.x, bB.endNode.x));
      }
      return result;
    });

    var y0 = nodes[0].y;

    var iminus = 0 ;
    var iplus = 0 ;  
    allBranchs.forEach(function(branch){

      if(mainPath.length === 0  || mainPath.indexOf(branch) < 0){
        var iterate = false;
        var new_y = 0 ;
        var x = getX_range(branch);

        allBranchs.forEach(function(branch2){
          var x2 = getX_range(branch2);
          if(((x2.x_min >= x.x_min  && x2.x_min <= x.x_max) || (x2.x_max >= x.x_min && x2.x_max <= x.x_max))
            && branch2.nodes[1].y === branch.nodes[1].y){
            iterate = true;
          }
        });
        if(iterate ||branch.nodes.length > 2){ // if(true)
          if(branchs.leftBranchs.indexOf(branch) < 0){
            iplus++;
            new_y = y0 + iplus*branch.nodes[1].dy/1.5;
          }
          else{
            iminus++;
            new_y = y0 - iminus*branch.nodes[1].dy/1.5;
          }

        }

        for(var j = 1 ; j < branch.nodes.length ; j++){
          branch.nodes[j].y = new_y;
        }

      }
    });
  }

function pushToTop(){

  var remainingNodes = nodes,
  dy = remainingNodes.length > 0 ? remainingNodes[1].dx : 0
  while(remainingNodes.length > 0){
    upperNodes = findUpperNodes(remainingNodes);
    var bound = upperNodes.y_min,
        remainingNodes = upperNodes.otherNodes,
        nextBound = findUpperNodes(remainingNodes).y_min;

    remainingNodes.forEach(function(node){
      node.y -= (nextBound - bound) - dy;
    })
  }

}

// Mauvais nom
function findUpperNodes(allNodes){

  var result = {"y_min": +Infinity, "otherNodes": []} ;
  allNodes.forEach(function(node){
    result.y_min = node.y < result.y_min ? node.y : result.y_min;
  })

  allNodes.forEach(function(node){
    if(Math.round(node.y) > Math.round(result.y_min)){
      result.otherNodes.push(node);
    }
  })

  return result;
}

function  alignBranch(allBranchs, forks, branch){

  var forkNode = getX_range(branch).x_min === branch.beginningNode.x ? branch.beginningNode : branch.endNode;

  forks.forEach(function(fork){
    if(fork.forkNode == forkNode){
      // On aligne avec la branche la plus grande
      var maxLength = 0,
          x = getX_range(branch);
          margin = (x.x_max - x.x_min)/branch.nodes.length;
          referenceBranch = {"nodes": [], "beginningNode": null, "endNode": null};
      fork.branchs.forEach(function(neighborBranch){

        if( !equalBranchs(branch, neighborBranch)
          && getX_range(neighborBranch).x_min < x.x_min && neighborBranch.nodes.length > maxLength){
          maxLength = neighborBranch.nodes.length;
          referenceBranch = neighborBranch;
        }
      })
      if(referenceBranch.nodes.length != 0){
        var newPlaceIsFree = true;
        allBranchs.forEach(function(branch2){
          var x2 = getX_range(branch2);
          if(((x2.x_min > x.x_min + margin && x2.x_min < x.x_max - margin) 
            || (x2.x_max > x.x_min + margin && x2.x_max < x.x_max - margin))
             && branch2.nodes[1].y === referenceBranch.nodes[1].y){
            newPlaceIsFree = false;
          }
        });
        if(newPlaceIsFree){
          for(var j = 1 ; j < branch.nodes.length ; j++){
            branch.nodes[j].y  = referenceBranch.nodes[1].y;
          }
        }

      }
    }
  });

}

function alignBranchs2(branchs){

  var allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs).concat(branchs.loops);
  
  allBranchs.forEach(function(branch){

    if(branch.nodes.length > 2 && branch.nodes[0].y !== branch.nodes[1].y){

      var dx = +Infinity;
      var x0 = getX_range(branch).x_min, x1 = getX_range(branch).x_max;
      for(var i = 0 ; i < branch.nodes.length - 1; i++){
        dx = Math.abs(branch.nodes[i].x - branch.nodes[i+1].x) < dx ? Math.abs(branch.nodes[i].x - branch.nodes[i+1].x) : dx;
      }

      var placeIsFree = true;
      while(x0 <= x1){
        nodes.forEach(function(node){

          if(branch.nodes.indexOf(node) < 0 && node.y === branch.nodes[0].y && node.x === x0){
            placeIsFree = false;
          }

          if(branch.beginningNode.name.localeCompare("GARE DE SAVIGNY SUR ORGE") === 0 
            || branch.endNode.name.localeCompare("GARE DE SAVIGNY SUR ORGE") === 0){
          }
        })
        x0 += dx;
      }
      if(placeIsFree){
        for(var i = 1 ; i < branch.nodes.length ; i++){
        branch.nodes[i].y = branch.nodes[0].y;
        }
      }
      else{
        placeIsFree = true;
        for(var i = 1 ; i < branch.nodes.length ; i++){
          nodes.forEach(function(node){
            if(node !== branch.nodes[i] && (node.y) === (branch.endNode.y) && (node.x) === (branch.nodes[i].x)){
              placeIsFree = false;
            }
          })
        }
        if(placeIsFree){
          for(var i = 1 ; i < branch.nodes.length ; i++){
          branch.nodes[i].y = branch.endNode.y;
          }
        }
      }
    }
  })

}

/** y1 la valeur maximale prise par la branche en y, x1 une valeur de x correspondante
  * y1 la valeur minimale prise par la branche en y, x0 une valeur de x correspondante
 **/
function getY_position(branch){

  var y1 = -Infinity , y0 = +Infinity, x1, x0;

  branch.nodes.forEach(function(node){
    if(node.y > y1){
      y1 = node.y;
      x1 = node.x;
    }
    else if (node.y < y0) {
      y0 = node.y;
      x0 = node.x;
    }
  })

  return {"y1": y1, "x1": x1, "y0": y0, "x0": x0}

}

function replaceNodes(){
  for(var i = 0 ; i < nodes.length ; i++){
    var possiblePlace = -1,
        isFree = true;
    for(var j = 0 ; j < nodes[i].sourceLinks.length ; j++){
      for(k = 0 ; k < nodes[i].targetLinks.length ; k++){
        if(getTarget(nodes[i].sourceLinks[j]).y !== nodes[i].y 
          && getTarget(nodes[i].sourceLinks[j]).y === getSource(nodes[i].targetLinks[k]).y){
          possiblePlace = getSource(nodes[i].targetLinks[k]).y;
        }
      }
    }
    if(nodes[i].sourceLinks.length > 1 && nodes[i].targetLinks.length === 0){
      for(var j = 0 ; j < nodes[i].sourceLinks.length ; j++){
        possiblePlace = getTarget(nodes[i].sourceLinks[j]).y !== nodes[i].y && (possiblePlace === -1
         || getTarget(nodes[i].sourceLinks[j]).y < possiblePlace)
        ? getTarget(nodes[i].sourceLinks[j]).y : possiblePlace;
      }
    }          
    if(possiblePlace !== -1){
      for(var j = 0 ; j < nodes.length ; j++){
        if(nodes[j].x === nodes[i].x && nodes[j].y === possiblePlace){
          isFree = false;
        }
      }
      if(isFree){
        nodes[i].y = possiblePlace;
      }
    }
  }

  var branchs = getBranchsAndForks().branchs;
  var allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs).concat(branchs.loops);
  allBranchs.forEach(function(branch){
    for(var i = 1 ; i < branch.nodes.length -1 ; i++){
      var aligned = false;
      branch.nodes.forEach(function(node){
        if(node.y === branch.nodes[i].y && node !== branch.nodes[i]){
          aligned = true;
        }
      });

      var aligned = false;
      branch.beginningNode.sourceLinks.forEach(function(sourceLink){
        if(getTarget(sourceLink).y === branch.beginningNode.y){
          aligned = true;
        }
      })

      branch.beginningNode.targetLinks.forEach(function(targetLink){
        if(getSource(targetLink).y === branch.beginningNode.y)
          aligned = true;
      })

      if(!aligned){
        branch.beginningNode.y = branch.nodes[1].y;
      }

      aligned = false;
      branch.endNode.sourceLinks.forEach(function(sourceLink){
        if(getTarget(sourceLink).y === branch.endNode.y){
          aligned = true;
        }
      })

      branch.endNode.targetLinks.forEach(function(targetLink){
        if(getSource(targetLink).y === branch.endNode.y)
          aligned = true;
      })

      if(!aligned){
        branch.endNode.y = branch.nodes[branch.nodes.length -2].y;
      }
    }
  })
}

function resolveCollisions(branchs){

  var allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs).concat(branchs.loops);

  allBranchs.forEach(function(branch){

    var dx = +Infinity;
      for(var i = 0 ; i < branch.nodes.length - 1; i++){
        dx = Math.abs(branch.nodes[i].x - branch.nodes[i+1].x) < dx ? Math.abs(branch.nodes[i].x - branch.nodes[i+1].x) : dx;
      }
    resolveCollisionsRec(branchs, branch, dx);
  })

}
/*
      var dx = +Infinity;
      var x0 = getX_range(branch).x_min, x1 = getX_range(branch).x_max;
      for(var i = 0 ; i < branch.nodes.length - 1; i++){
        dx = Math.abs(branch.nodes[i] - branch.nodes[i+1]) < dx ? Math.abs(branch.nodes[i] - branch.nodes[i+1]) : dx;
      }


          if(branch.nodes.indexOf(node) < 0 && node.y === branch.nodes[0].y && node.x === x0){
            placeIsFree = false;
          }
*/

function resolveCollisionsRec(branchs, branch, dx){
  var push = false;
  var up = (branchs.leftBranchs.indexOf(branch) > -1) ;

  nodes.forEach(function(node){
    var x0 = getX_range(branch).x_min, x1 = getX_range(branch).x_max;
    for(var i = 1 ; i < branch.nodes.length ; i++){
      if(node !== branch.nodes[i] && Math.round(node.y) === Math.round(branch.nodes[i].y) && node.x === branch.nodes[i].x){
        push = true;
      }
    }

  })

  if(up && push){
    for(var i = 1 ; i < branch.nodes.length ; i++){
      branch.nodes[i].y -= 2*branch.nodes[0].dx/1.5;
    }
    resolveCollisionsRec(branchs, branch, dx);
  }
  else if(push){
    for(var i = 1 ; i < branch.nodes.length ; i++){
      branch.nodes[i].y += 2*branch.nodes[0].dx/1.5;
    }
    resolveCollisionsRec(branchs, branch, dx);    
  }

}

function untangleBranchsVertically(branchs){

  var allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs).concat(branchs.loops);
    var untangled = false;
  for(var a = 0 ; a < allBranchs.length - 1; a++){
    for(var b = a + 1 ; b < allBranchs.length ; b++){
      var branchA = allBranchs[a];
      var branchB = allBranchs[b];
      var changeAandB = false;

      var xA = getX_range(branchA),
          xB = getX_range(branchB),
          xA0 = [] , xA1 = [] , yA0 = [] , yA1 = [],
          xB0 = [] , xB1 = [] , yB0 = [] , yB1 = [];
      var nameA0 = [], nameA1 = [], nameB0 = [], nameB1 = [];

      // Intersection possible
      if((xA.x_min <= xB.x_min && xB.x_min <= xA.x_max) || (xA.x_min <= xB.x_max && xB.x_max <= xA.x_max)
        || (xB.x_min <= xA.x_min && xA.x_min <= xB.x_max) || (xB.x_min <= xA.x_max && xA.x_max <= xB.x_max)){

        for(var i = 0 ; i < branchA.nodes.length - 1 ; i++){
          if(branchA.nodes[i].y != branchA.nodes[i+1].y){
            xA0.push(branchA.nodes[i].x);
            xA1.push(branchA.nodes[i+1].x);
            yA0.push(branchA.nodes[i].y);
            yA1.push(branchA.nodes[i+1].y);
            nameA0.push(branchA.nodes[i].name);
            nameA1.push(branchA.nodes[i+1].name);
          }
        }
        for(var i = 0 ; i < branchB.nodes.length - 1 ; i++){
          if(branchB.nodes[i].y != branchB.nodes[i+1].y){
            xB0.push(branchB.nodes[i].x);
            xB1.push(branchB.nodes[i+1].x);
            yB0.push(branchB.nodes[i].y);
            yB1.push(branchB.nodes[i+1].y);
            nameB0.push(branchB.nodes[i].name);
            nameB1.push(branchB.nodes[i+1].name);
          }
        }

        if(xA0.length > 0){
          for(var i = 0 ; i < xA0.length ; i++){
            var y0 = Infinity, y1 = Infinity;
            for(var j = 0 ; j < branchB.nodes.length ; j++){
              if(branchB.nodes[j].x === xA0[i]){
                y0 = branchB.nodes[j].y;
              }
              else if(branchB.nodes[j].x === xA1[i]){
                y1 = branchB.nodes[j].y;
              }
            } 

            if(y0 != Infinity && y1 != Infinity && Math.min(yA0[i], yA1[i]) < Math.min(y0, y1) && Math.max(yA0[i], yA1[i]) > Math.max(y0, y1)){
              changeAandB = true;
              untangled = true;
            }               
          }     
        }

        if(xB0.length > 0){
          for(var i = 0 ; i < xB0.length ; i++){
            var y0 = Infinity, y1 = Infinity;
            for(var j = 0 ; j < branchA.nodes.length ; j++){
              if(branchA.nodes[j].x === xB0[i]){
                y0 = branchA.nodes[j].y;
              }
              else if(branchA.nodes[j].x === xB1[i]){
                y1 = branchA.nodes[j].y;
              }
            } 
            if(y0 != Infinity && y1 != Infinity && 
              Math.min(yB0[i], yB1[i]) < Math.min(y0, y1) && Math.max(yB0[i], yB1[i]) > Math.max(y0, y1)){
              changeAandB = true;
              untangled = true;
            }               
          }     
        }
      }

      if(changeAandB){
        swapBranchs(branchA, branchB);
      }
    }
  }

  function swapBranchs(branchA, branchB){
    var yAValues = [];
    var nbANodes = [];
    for(var i = 1 ; i < branchA.nodes.length ; i++){
      if(yAValues.indexOf(branchA.nodes[i].y) > - 1){
        nbANodes[yAValues.indexOf(branchA.nodes[i].y)] += 1;
      }
      else{
        yAValues.push(branchA.nodes[i].y);
        nbANodes.push(1);
      }
    }
    var yBValues = [];
    var nbBNodes = [];
    for(var i = 1 ; i < branchB.nodes.length ; i++){
      if(yBValues.indexOf(branchB.nodes[i].y) > - 1){
        nbBNodes[yBValues.indexOf(branchB.nodes[i].y)]++;
      }
      else{
        yBValues.push(branchB.nodes[i].y);
        nbBNodes.push(1);
      }
    }
    var yA = yAValues[nbANodes.indexOf(Math.max.apply(null, nbANodes))],
        yB = yBValues[nbBNodes.indexOf(Math.max.apply(null, nbBNodes))];
    for(var i = 1 ; i < branchA.nodes.length ; i++){
      branchA.nodes[i].y = branchA.nodes[i].y === yA ? yB : branchA.nodes[i].y;
    }
    for(var i = 1 ; i < branchB.nodes.length ; i++){
      branchB.nodes[i].y = branchB.nodes[i].y === yB ? yA : branchB.nodes[i].y;
    }
  }

  return untangled;
}


function untangleBranchsHorizontally(forks){

  var changedBranchs = new Set();
  var untangled = false;

  forks.forEach(function(forkA){
    forks.forEach(function(forkB){
      if(forkA !== forkB){
        forkA.branchs.forEach(function(branchA){
            forkB.branchs.forEach(function(branchB){

              var commonNode = branchA.beginningNode === branchB.beginningNode || branchA.endNode === branchB.endNode
                              || branchA.beginningNode === branchB.beginningNode || branchA.endNode === branchB.beginningNode;

              if(!equalBranchs(branchA, branchB) && !commonNode){
                var y1A, y0A, x1A, x0A;
                var y_x = getY_position(branchA);
                y1A = y_x.y1; 
                y0A = y_x.y0;
                x1A = y_x.x1;
                x0A = y_x.x0;

                minXA = getX_range(branchA).x_min,
                maxXA = getX_range(branchA).x_max;
                minYA = Math.min(y0A, y1A),
                maxYA = Math.max(y0A, y1A), 
                aA = (y1A-y0A)/(x1A-x0A),
                bA = (y0A*x1A - y1A*x0A)/(x1A - x0A);
            if(minYA !== maxYA){

                var y1B, y0B, x1B, x0B;
                y_x = getY_position(branchB);
                y1B = y_x.y1; 
                y0B = y_x.y0;
                x1B = y_x.x1;
                x0B = y_x.x0;
                    minXB = getX_range(branchB).x_min,
                    maxXB = getX_range(branchB).x_max;
                    minYB = Math.min(y0A, y1A),
                    maxYB = Math.max(y0A, y1A)
                    aB = (y1B-y0B)/(x1B-x0B),
                    bB = (y0B*x1B - y1B*x0B)/(x1B - x0B);
                var xsol = (bB - bA) / (aA - aB);
             //   if( ((minXA < minXB && minXB < maxXA) || (minXB < minXA && minXA < maxXB) || (minXA < maxXB && maxXB < maxXA) || (minXB < maxXA && maxXA < maxXB)
             //     && ((minYA < minYB && minYB < maxYA) || (minYB < minYA && minYA < maxYB) || (minYA < maxYB && maxYB < maxYA) || minYB < maxYA && maxYA < maxYB))){
                  minXA = Math.round(minXA);
                  minXB = Math.round(minXB);
                  maxXA = Math.round(maxXA);
                  maxXB = Math.round(maxXB);
                  xsol = Math.round(xsol);
                if(aA !== aB && (minXA < xsol && xsol < maxXA) && (minXB < xsol && xsol < maxXB)
                /*  && done <2/*(!changedBranchs.has(branchA) || !changedBranchs.has(branchB))*/){
                
                  untangled = true;
                  changedBranchs.add(branchA);
                  changedBranchs.add(branchB);
                  yA = getY_position(branchA);
                  yB = getY_position(branchB);
                  var branchToPush = yA.x0 < yB.x0 ? branchB : branchA;

                  if(branchs.leftBranchs.indexOf(branchToPush) >= 0){
                    pushBranchUp(branchToPush, branchs);
                  }
                  else{
                    pushBranchDown(branchToPush, branchs);
                  }
         //         }
                }
              }
            }
          })     
        })
      }
    })
  })

  return untangled;

  function pushBranchUp(branch0, branchs){

    var y_min = +Infinity,
        y_max,
        x0_min,
        x0_max;

    var allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs).concat(branchs.loops);

    nodes.forEach(function(node){
      y_min = node.y < y_min ? node.y : y_min;
    })
    x0_min = getX_range(branch0).x_min;
    x0_max = getX_range(branch0).x_max;
    y_max = branch0.nodes[1].y;

    var visitedBranchs = new Set();

    allBranchs.forEach(function(branch){

      if(!equalBranchs(branch0, branch) && !visitedBranchs.has(branch)){
        visitedBranchs.add(branch)
        var x1_min, x1_max, y;
        x1_min = getX_range(branch).x_min;
        x1_max = getX_range(branch).x_max;
        y = branch.nodes[1].y;

        if(((x0_min <= x1_min && x1_min <= x0_max) || (x0_min <= x1_min && x1_min <= x0_max) 
          || (x1_min <= x0_min && x0_min <= x1_max) || (x1_min <= x0_min && x0_min <= x1_max))
          && y_max > y){

          for(var i = 0 ; i < branch.nodes.length ; i++){
            if(branch.nodes[i].y < y_max){
              branch.nodes[i].y += 2*branch.nodes[0].dx/1.5;
            }
          }
        }
      }
    })

    for(var i = 1 ; i < branch0.nodes.length ; i++){
      branch0.nodes[i].y = y_min;
    }

  }

  function pushBranchDown(branch0, branchs){

    var y_max = -Infinity,
        y_min,
        x0_min,
        x0_max;

    var allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs).concat(branchs.loops);

    nodes.forEach(function(node){
      y_max = node.y > y_max ? node.y : y_max;
    })
    x0_min = getX_range(branch0).x_min;
    x0_max = getX_range(branch0).x_max;
    y_min = branch0.nodes[1].y;

    var visitedBranchs = new Set();

    allBranchs.forEach(function(branch){

      if(!equalBranchs(branch0, branch) && !visitedBranchs.has(branch)){
        visitedBranchs.add(branch)
        var x1_min, x1_max, y;
        x1_min = getX_range(branch).x_min;
        x1_max = getX_range(branch).x_max;
        y = branch.nodes[1].y;

        if(((x0_min <= x1_min && x1_min <= x0_max) || (x0_min <= x1_min && x1_min <= x0_max))
          && y_min < y){

          for(var i = 0 ; i < branch.nodes.length ; i++){
            if(branch.nodes[i].y > y_min){
              branch.nodes[i].y -= 2*branch.nodes[0].dx/1.5;
            }
          }
        }
      }
    })

    for(var i = 1 ; i < branch0.nodes.length ; i++){
      branch0.nodes[i].y = y_max;
    }
  }

}

/**
 * branch = {nodes: [node], beginningNode: node, endNode: node, type: "leftBranch"/"rightBranch"/"loop" }
 * fork = {branchs: [branch], forkNode: node}
 **/

function getX_range(branch){
  return {"x_min": Math.min(branch.beginningNode.x, branch.endNode.x), "x_max": Math.max(branch.beginningNode.x, branch.endNode.x)}
}


// Attention si target n'est pas un forkNode, il faut prendre le beginning node de la branche qui le contient 
// findMainPath renvoie une liste vide dans le cas où on a des boucles, sinon, il renvoie la liste des branches le constituant
function findMainPath(branchs, graph){

  var allBranchs = branchs.rightBranchs.concat(branchs.leftBranchs);

  var sources = new Set(),
      targets = new Set(),
      paths = [];

  var x_min = Infinity,
      x_max = 0;
  nodes.forEach(function(node){
    x_min = node.x < x_min ? node.x : x_min;
    x_max = node.x > x_max ? node.x : x_max;
  })
  nodes.forEach(function(node){
    if(node.x == x_min){
      sources.add(findNearestForkNode(node, allBranchs));
    }
    else if(node.x == x_max){
      targets.add(findNearestForkNode(node, allBranchs));
    }
  })

  sources.forEach(function(source){
    targets.forEach(function(target){
      var sourceToTarget = getPaths(source, target, graph, branchs);     
      sourceToTarget.forEach(function(path){
        if(path.length > 1 && validPath(path)){ 
          paths.push(path);
        }
      });
    });
  });

  var maxLength = 0,
      mainPath = [];

  paths.forEach(function(path){
    var pathLength = 0 ;
    path.forEach(function(branch){
      pathLength += branch.nodes.length;
    })
    if(pathLength > maxLength){
      maxLength = pathLength;
      mainPath = path;
    }
  })

  return mainPath;

  function findNearestForkNode(node, allBranchs){
    var isForkNode = false;
    var nearestNode = node
    allBranchs.forEach(function(branch){
      if(branch.beginningNode == node || branch.endNode == node){
        isForkNode = true ; 
      }
    })
    if(!isForkNode){
      allBranchs.forEach(function(branch){
        var isInBranch = false;
        branch.nodes.forEach(function(branchNode){
          if(branchNode == node){
            isInBranch = true;
          }
        })
        if(isInBranch){
          nearestNode = branch.beginningNode;
        }
      })
    }
    return nearestNode;
  }

  function validPath(path){

    var isValid = true;

    if(!path.length === 1){

      var i_index = 0,
          iplus_index = 0;

      for(var i = 0 ; i < path.length ; i++){
        if(path[i].beginningNode == path[i+1].beginningNode){
          i_index = 1;
          iplus_index = 1;
       }
        else if(path[i].beginningNode == path[i+1].endNode){
          i_index = 1 ;
          iplus_index = path[i+1].nodes.length - 2;
        }
        else if(path[i].endNode == path[i+1].beginningNode){
          i_index = path[i+1].nodes.length - 2;
          iplus_index = 1;
        }
        else if(path[i].endNode == path[i+1].endNode){
          i_index = path[i].nodes.length -2;
          iplus_index = path[i+1].nodes.length -2;
        }
        if(path[i].nodes[i_index].x >= path[iplus_index].nodes[1].x){
          isValid = false;
        }
      }      
    }
    return isValid;
  }


}


function reverseBranch(branch){

  var reversedNodes = [];
  for(var i = 0 ; i < branch.nodes.length ; i++){
    reversedNodes.push(branch.nodes[branch.nodes.length - i - 1]);
  }
  return {"nodes": reversedNodes, "beginningNode": branch.endNode, "endNode": branch.beginningNode};
}

function equalNodes(nodesA, nodesB){

  var equal;
  if(nodesA.length !== nodesB.length){
    equal = false;
  }
  else{
    equal = true;
    for(var i = 0 ; i < nodesA.length ; i++){
      if(nodesA[i].name.localeCompare(nodesB[i].name) !== 0){
        equal = false;
      }
    }
  }
  return equal;
}

/** 
  * Move branch to y position
  **/
function moveBranch(branch, y){

}

    function ascendingDepth(a, b) {
      return a.y - b.y;
    }
  }

  function computeLinkDepths() {
    nodes.forEach(function(node) {
      node.sourceLinks.sort(ascendingTargetDepth);
      node.targetLinks.sort(ascendingSourceDepth);
    });
    nodes.forEach(function(node) {
      var sy = 0, ty = 0;
      node.sourceLinks.forEach(function(link) {
        link.sy = sy;
        sy += link.dy;
      });
      node.targetLinks.forEach(function(link) {
        link.ty = ty;
        ty += link.dy;
      });
    });

    function ascendingSourceDepth(a, b) {
      return a.source.y - b.source.y;
    }

    function ascendingTargetDepth(a, b) {
      return a.target.y - b.target.y;
    }
  }

  function center(node) {
    return node.y + node.dy / 2;
  }

  function value(link) {
    return link.value;
  }

  /* Cycle Related computations */
  function markCycles() {
    // ideally, find the 'feedback arc set' and remove them.
    // This way is expensive, but should be fine for small numbers of links
    var cycleMakers = [];
    var addedLinks = new Array();
    links.forEach(function(link) {
      if( createsCycle( link.source, link.target, addedLinks ) ) {
      link.causesCycle=true;
    link.cycleIndex = cycleMakers.length;
        cycleMakers.push( link );
      } else {
        addedLinks.push(link);
      }
    });
  };


  function createsCycle( originalSource, nodeToCheck, graph ) {
    if( graph.length == 0 ) {
      return false;
    }

    var nextLinks = findLinksOutward( nodeToCheck, graph );
    // leaf node check
    if( nextLinks.length == 0 ) {
      return false;
    }

    // cycle check
    for( var i = 0; i < nextLinks.length; i++ ) {
      var nextLink = nextLinks[i];

      if( nextLink.target === originalSource ) {
        return true;
      }

      // Recurse
      if( createsCycle( originalSource, nextLink.target, graph ) ) {
        return true;
      }
    }

    // Exhausted all links
    return false;
  };

  /* Given a node, find all links for which this is a source
     in the current 'known' graph  */
  function findLinksOutward( node, graph ) {
    var children = [];

    for( var i = 0; i < graph.length; i++ ) {
      if( node == graph[i].source ) {
        children.push( graph[i] );
      }
    }

    return children;
  }


  return sankey;
};
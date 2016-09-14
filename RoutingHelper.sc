
RoutingHelper {

  var <>front, <>side, <>rear, <>centre; 

  calculateRouting { arg numOfChannels = 2;
    if ( numOfChannels == 2, {
      front = [0,1];
      side = [0,1];
      rear = [0,1];
      centre = [0,1]; 
    });

    if ( numOfChannels == 4, {
      front = [0,1];
      side = [2,3];
      rear = [2,3];
      centre = [0,1]; 
    });

    if ( numOfChannels == 6, {
      front = [0,1];
      side = [2,3];
      rear = [4,5];
      centre = [0,1]; 
    });

    if ( numOfChannels == 8, {
      front = [0,1];
      side = [2,3];
      rear = [4,5];
      centre = [6,7]; 
    }); 
  }

}

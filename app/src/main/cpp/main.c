#include <stdio.h>
#include <stdlib.h>
//#include "GPX.h"
//#include "Waypoint.h"
#include "include/SimpleCPXParser.h"
//#include "Location.h"

int main(int argc, char **argv){
  //printf("%s", argv[1]);
  GPX *gpx = parse_GPX(argv[1]);
  printf("%s", gpx->creator);
  Waypoint *wpt = gpx->waypoints;
  while (wpt){
    Location *location = wpt->location;
    printf("%s: %f | %f\n", wpt->name, location->latitude, location->longitude);
    wpt = wpt->next;
  }
  Track *trk = gpx->tracks;
  while (trk){
    Track_Seg *trkseg = trk->track_segs;
    printf("Track: %s\n", trk->name);
    while (trkseg){
      Location *location = trkseg->locations;
      while (location){
        printf("Location: %f, %f", location->latitude, location->longitude);
        location = location->next;
      }
      trkseg = trkseg->next;
    }
    trk = trk->next;
  }
  free(gpx->creator);
  free(gpx);
  return 0;
}

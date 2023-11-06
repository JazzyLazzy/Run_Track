#include "expat.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <android/log.h>
#include "GPX.h"
#include "Waypoint.h"
#include "Location.h"
#include "SimpleCPXParser.h"

#define BUFFSIZE 8192


bool is_wpt = false;
char buff[BUFFSIZE];
int d;
bool is_name = false;
bool is_trk = false;
bool is_trkseg = false;
bool is_time = false;
bool is_ele = false;
bool is_trkpt = false;

void initialise_gpx(void *userData, const char *name, const char *args[]){
  if (strncmp(name, "gpx\0", 4) != 0){
     printf("N'est pas gpx valide.");
     exit(1);
  }
  GPX *gpx = userData;

  for (int i = 0; args[i]; i+=2){
//    printf("%d", i);
    if (strncmp(args[i], "creator\0", 8) == 0){
  //    printf("%s: %s", args[i], args[i+1]);
      gpx->creator = malloc(sizeof (args[i+1]) + 1);
      strcpy(gpx->creator, args[i+1]);
    }else if(strncmp(args[i], "version\0", 8) == 0){
      strncpy(gpx->version, args[i+1], 4);
    }
  }
}

void set_location(Location *location, const char *args[]){
  for (int i=0; args[i]; i+=2){
    if (strncmp(args[i], "lat\0", 4) == 0){
      location->latitude = atof(args[i+1]);
    }else if(strncmp(args[i], "lon\0", 4) == 0){
      location->longitude = atof(args[i+1]);
    }
  }
}

void handle_waypoint(void *userData, const char *args[]){
  GPX *gpx = userData;
  Waypoint *wpt = malloc(sizeof (Waypoint));
  if (gpx->waypoints == NULL){
    wpt->next = NULL;
    gpx->waypoints = wpt;
    gpx->size = 1;
  }else{
    wpt->next = gpx->waypoints;
    gpx->waypoints = wpt;
    gpx->size++;
  }
  Location *location = malloc(sizeof (Location));
  location->next = NULL;
  wpt->location = location;
  set_location(location, args);
}

void handle_trk(void *userData){
  GPX *gpx = userData;
  Track *trk = malloc(sizeof (Track));
  trk->next = NULL;
  trk->track_segs = NULL;
  if (gpx->tracks == NULL){
    trk->next = NULL;
    gpx->tracks = trk;
  }else{
    trk->next = gpx->tracks;
    gpx->tracks = trk;
  }
}

void handle_trkseg(void *userData){
  GPX *gpx = userData;
  Track_Seg *trkseg = malloc(sizeof (Track_Seg));
  trkseg->next = NULL;
  trkseg->locations = NULL;
  if (gpx->tracks->track_segs == NULL){
    trkseg->next = NULL;
    gpx->tracks->track_segs = trkseg;
  }else{
    trkseg->next = gpx->tracks->track_segs;
    gpx->tracks->track_segs = trkseg;
  }
}

void handle_trkpt(void *userData, const char *args[]){
  GPX *gpx = userData;
  Location *location = malloc(sizeof (Location));
  location->next = NULL;
  set_location(location, args);
  if (gpx->tracks->track_segs->locations == NULL){
    location->next = NULL;
    gpx->tracks->track_segs->locations = location;
  }else{
    location->next = gpx->tracks->track_segs->locations;
    gpx->tracks->track_segs->locations = location;
  }
}

void handle_ele(void *userData, const char *args[]){
  GPX *gpx = userData;
  
}

void start_element(void *userData, const char *name, const char *args[]){
  if (d == 0) {
    initialise_gpx(userData, name, args);
  }else if (strncmp(name, "wpt\0", 4) == 0){
    is_wpt = true;
    handle_waypoint(userData, args);
  } else if (is_wpt && strncmp(name, "name\0", 5) == 0){
    is_name = true;
  }else if (strncmp(name, "trk\0", 4) == 0){
    is_trk = true;
    handle_trk(userData);
  }else if (is_trk && strncmp(name, "name\0", 5) == 0){
    is_name = true;
  }else if (is_trk && strncmp(name, "trkseg\0", 7) == 0){
    is_trkseg = true;
    handle_trkseg(userData);
  }else if (is_trkseg && strncmp(name, "trkpt\0", 6) == 0){
    is_trkpt = true;
    handle_trkpt(userData, args);
  }else if (is_trkpt && strncmp(name, "ele\0", 4) == 0){
    is_ele = true;
  }else if (is_trkpt && strncmp(name, "time\0", 5) == 0){
    is_time = true;
  } else {
    printf("Problème avec la lecture du fichier GPX. Format mauvais.");
  }
  d++;
}

void read_text(void *userData, const XML_Char *s, int len){
  GPX *gpx = userData; 
  if (is_wpt && is_name){
    char *name = malloc(sizeof (XML_Char) * (len + 1));
    strncpy(name, s, len);
    gpx->waypoints->name = name;
  }else if(is_trk && is_name){
    char *name = malloc(sizeof (XML_Char) * (len + 1));
    strncpy(name, s, len);
    gpx->tracks->name = name;
  }else if(is_ele && is_trkpt){
    double ele = atof(s);
    gpx->tracks->track_segs->locations->elevation = ele;
  }
}

void end_element(void *userData, const char *name){
  if (strncmp(name, "wpt\0", 4) == 0){
    is_wpt = false;
  }else if (strncmp(name, "name\0", 5) == 0){
    is_name = false;      
  }else if (strncmp(name, "trk\0", 4) == 0){
    is_trk = false;
  }else if (strncmp(name, "name\0", 5) == 0){
    is_name = false;
  }else if (strncmp(name, "trkseg\0", 7) == 0){
    is_trkseg = false;
  }else if (strncmp(name, "trkpt\0", 6) == 0){
    is_trkpt = false;
  }else if (strncmp(name, "ele\0", 4) == 0){
    is_ele = false;
  }else if (strncmp(name, "time\0", 5) == 0){
    is_time = false;
  }
  d--;
}

GPX *parse_GPX(char *file){

  //printf("hello parser");
  //printf("%s", file);
  //is_wpt = false;
  XML_Parser parser = XML_ParserCreate(NULL);
  
  if (!parser) {
    fprintf(stderr, "N'était pas possible d'allouer de la mémoire pour l'analyseur.\n");
    exit(-1);
  }

  XML_SetElementHandler(parser, start_element, end_element);
  XML_SetCharacterDataHandler(parser, read_text);
 
  GPX *gpx = malloc(sizeof (GPX));
  if (gpx == NULL){
      return NULL;
  } else {
      gpx->waypoints = NULL;
      gpx->tracks = NULL;
      gpx->size = 0;
  }

  XML_SetUserData(parser, gpx);

  FILE *gpxf = fopen(file, "r");
  if (!gpxf) {
    perror("Failed to open file");
    free(gpx);
    XML_ParserFree(parser);
    return NULL;
  }
  for (;;) {
    void *buff = XML_GetBuffer(parser, BUFFSIZE);
    int bytes_read = fread(buff, sizeof(char), BUFFSIZE, gpxf);
    int done = bytes_read < BUFFSIZE;
  

    if (XML_ParseBuffer(parser, bytes_read, done) == XML_STATUS_ERROR){
      fprintf(stderr, "Erreur d'analyse à la ligne %lu:\n%s\n",
      XML_GetCurrentLineNumber(parser),
      XML_ErrorString(XML_GetErrorCode(parser)));
      exit(1);
    }

    if (done) break;
  }
  fclose(gpxf);
  //printf("finish parse");
  //printf("%s", gpx->creator); 
  //free(gpx->creator);
  //free(gpx);
  XML_ParserFree(parser);
  return gpx;
  //
}

void free_gpx(GPX *gpx){
  printf("free bird");
  Waypoint *wpt = gpx->waypoints;
  while (wpt != NULL){
      //Waypoint *wpt_to_free = wpt;
      wpt = wpt->next;
      //free(wpt_to_free->location);
      //free(wpt_to_free);
  }
  Track *trk = gpx->tracks;
  while (trk){
    Track_Seg *track_seg = trk->track_segs;
    while(track_seg){
      Location *loc = track_seg->locations;
      while (loc){
        Location *loc_free = loc;
        loc = loc->next;
        free(loc_free);
      }
      Track_Seg *trkseg_free = track_seg;
      track_seg = track_seg->next;
      free(trkseg_free);
    }
    free(trk->name);
    Track *trk_free = trk;
    trk = trk->next;
    free(trk_free);
  }
  free(gpx->creator);
  free(gpx);
}

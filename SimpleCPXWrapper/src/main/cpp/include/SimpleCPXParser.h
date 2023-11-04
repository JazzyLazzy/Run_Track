
#ifndef SimpleCPXParser_H
#define SimpleCPXParser_H

#include "GPX.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Parse a GPX file.
 *
 * @param file The path to the GPX file to be parsed.
 */
GPX *parse_GPX(char *file);

void free_gpx(GPX *gpx);

#ifdef __cplusplus
}
#endif

#endif /* SimpleCPXParser_H*/


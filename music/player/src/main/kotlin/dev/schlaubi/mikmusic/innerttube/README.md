# innertube

This contains some mapping of the internal YouTube API to retrieve, auto-completion and chapters.

Huge thanks to [@Rxsto](https://rxs.to) for helping me to figure this out

<details>
    <summary>YouTube Music auto complete</summary>

POST `https://music.youtube.com/youtubei/v1/music/get_search_suggestions`

```json
{
  "input": "<query>",
  "context": {
    "client": {
      "clientName": "WEB_REMIX",
      "clientVersion": "1.20220502.01.00"
    }
  }
}
```

Response

```json
{
  "contents": [
    {
      "searchSuggestionsSectionRenderer": {
        "contents": [
          {
            "searchSuggestionRenderer": {
              "suggestion": {
                "runs": [
                  {
                    "text": "abba",
                    "bold": true
                  }
                ]
              }
            }
          },
          {
            "searchSuggestionRenderer": {
              "suggestion": {
                "runs": [
                  {
                    "text": "abba",
                    "bold": true
                  },
                  {
                    "text": " neue songs 2021"
                  }
                ]
              }
            }
          },
          {
            "searchSuggestionRenderer": {
              "suggestion": {
                "runs": [
                  {
                    "text": "abba",
                    "bold": true
                  },
                  {
                    "text": " songs"
                  }
                ]
              }
            }
          },
          {
            "searchSuggestionRenderer": {
              "suggestion": {
                "runs": [
                  {
                    "text": "abba",
                    "bold": true
                  },
                  {
                    "text": " dancing queen"
                  }
                ]
              }
            }
          },
          {
            "searchSuggestionRenderer": {
              "suggestion": {
                "runs": [
                  {
                    "text": "abba",
                    "bold": true
                  },
                  {
                    "text": " gimme gimme gimme"
                  }
                ]
              }
            }
          },
          {
            "searchSuggestionRenderer": {
              "suggestion": {
                "runs": [
                  {
                    "text": "abba",
                    "bold": true
                  },
                  {
                    "text": " voyage"
                  }
                ]
              }
            }
          },
          {
            "searchSuggestionRenderer": {
              "suggestion": {
                "runs": [
                  {
                    "text": "abba",
                    "bold": true
                  },
                  {
                    "text": " chiquitita"
                  }
                ]
              }
            }
          }
        ],
        "trackingParams": "CAAQi24iEwi3rouXgdb3AhXpRXoFHVGlAXU="
      }
    }
  ]
}
```

joining `/contents/0/searchSuggestionsSectionRenderer/contents/X/searchSuggestionRenderer/suggestion/runs/text`
represents a single result item

the bold parameter is used to highlight a section of the result in the YouTube UI
</details>

<details>
<summary>Chapters</summary>

POST `https://www.youtube.com/youtubei/v1/search`

```json5
{
  "context": {
    "client": {
      "clientName": "WEB",
      "clientVersion": "2.20220502.01.00"
    }
  },
  // video ID
  "query": "mKowdt2eDI8"
}
```

Response

```json5
{
  "contents": {
    "twoColumnSearchResultsRenderer": {
      "primaryContents": {
        "sectionListRenderer": {
          "contents": [
            {
              "itemSectionRenderer": {
                "contents": [
                  {
                    "videoRenderer": {
                      // search for video ID
                      "videoId": "mKowdt2eDI8",
                      "expandableMetadata": {
                        "expandableMetadataRenderer": {
                          "expandedContent": {
                            "horizontalCardListRenderer": {
                              "cards": [
                                {
                                  "macroMarkersListItemRenderer": {
                                    "title": {
                                      "simpleText": "1. It Has Begun"
                                    },
                                    "timeDescription": {
                                      "simpleText": "0:00"
                                    },
                                  }
                                },
                                {
                                  "macroMarkersListItemRenderer": {
                                    "title": {
                                      "simpleText": "2. The Mad King"
                                    },
                                    "timeDescription": {
                                      "simpleText": "2:50"
                                    },
                                  }
                                },
                              ],
                            }
                          },
                        }
                      }
                    }
                  },
                ],
              }
            },
          ],
        }
      }
    }
  },
}
```

Search for first match
at `/contents/twoColumnSearchResultsRenderer/primaryContents/sectionListRenderer/contents/0/itemSectionRenderer/contents/X/videoRenderer/videoId`

Then
read `/contents/twoColumnSearchResultsRenderer/primaryContents/sectionListRenderer/contents/0/itemSectionRenderer/contents/0/videoRenderer/expandableMetadata/expandableMetadataRenderer/expandedContent/horizontalCardListRenderer/cards/X/macroMarkersListItemRenderer`
</details>

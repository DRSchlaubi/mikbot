package dev.schlaubi.mikbot.util_plugins.ktor

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureRedoc() {
    get("docs") {
        call.respondText(ContentType.Text.Html) {
            //language=HTML
            """
                <!DOCTYPE html>
                <html lang='en'>
                  <head>
                    <title>Redoc</title>
                    <meta charset="utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">

                    <style>
                      body {
                        margin: 0;
                        padding: 0;
                      }
                    </style>
                  </head>
                  <body>
                    <redoc spec-url='openapi.json'></redoc>
                    <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"> </script>
                  </body>
                </html>
            """.trimIndent()
        }
    }
}

# GDPR

Plugin adding functionality to comply with the [GDPR](https://gdpr.eu/)

## API

This plugin provides an API for other plugins, so they can comply with the GDPR more easily.

The API consists of so called `DataPoints`, each data point represents one type of data collected by the providing
plugin. Depending on what type of data point it is, it needs to provide methods of requesting and deleting the data.

### Example Extension

In order to register your data points, please refer to the `GDPRExtensionPoint` and
the [PF4J Documentation](https://pf4j.org/doc/extensions.html)

```kotlin 
@Extension
class MyGDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> = listOf(dataPoint1, dataPoint2, dataPoint3)
}
```

For an example data point, you can check
the [database-i18n plugin](https://github.com/DRSchlaubi/mikmusic/blob/3dc82da7ef5dca15c6e75268cae2935cad52f3f7/core/database-i18n/src/main/kotlin/dev/schlaubi/mikbot/core/i18n/database/gdpr/GDPR.kt#L17-L26)

You can also refer to the [DataPoint.kt file](https://github.com/DRSchlaubi/mikbot/blob/main/core/gdpr/src/main/kotlin/dev/schlaubi/mikbot/core/gdpr/api/DataPoint.kt)
to learn more about data points and their different types

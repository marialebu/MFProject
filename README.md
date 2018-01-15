# Modelo para la detección de anomalías. 

Este software es un producto del proyecto *"SIEM: Plataforma para la detección de ataques en tiempo real a partir de eventos de seguridad"*. La implementación se hizo a través de un aplicativo en Java que permite:

1. Cargar los datos que se extraigan del SIEM del CCOC para ser analizados. En el proceso la aplicación filtra automáticamente las columnas innecesarias, realiza la conversión para que la información sea leída por Weka, se analiza el archivo y las instancias que puedan ser predichas se predicen y se muestra el resultado. Las instancias que no puedan ser clasificadas se exportan a un archivo CSV para que el operador del CCOC las clasifique y las importe al aplicativo para que la aplicación aprenda aún más.
2. Cargar los datos que se extraigan del SIEM del CCOC o que se exportaron en la funcionalidad anterior para que el modelo aprenda. En el proceso la aplicación filtra automáticamente las columnas innecesarias, realiza la conversión para que la información sea leída por Weka y se construye un nuevo modelo a partir de la información nueva y la histórica.

Se espera que en el futuro se pueda desarrollar un aplicativo más avanzado que permita la interacción directa del SIEM con la misma sin necesidad de la carga manual de un operador.

## Realizar una predicción: 
1. Abra el programa y haga clic en el botón de abrir archivo, aparecerá un diálogo donde se debe cargar un archivo .csv. Procure que los eventos que aparezcan en dicho archivo tengan al menos los siguientes atributos: Type, Transport Protocol, Aggregated Event Count, Priority, Agent Severity, Device Event Category, Device Action, Device Vendor, Attacker Address, Attacker Port, Attacker Geo Country Name, Target Address, Target Translated Address, Target Port, Target Service Name, Target Geo Location Info, Target Geo Country Name, Request Url, Device Custom String1, Device Custom String3, Device Custom String4. 
2. 
![Imagen 1](http://i68.tinypic.com/2f04t43.png)
2. Una vez seleccionado el archivo el programa pregunta qué delimitador tiene el archivo csv, en este caso será un “;”. 
![](http://i64.tinypic.com/33w5s0i.png)
3. Hacemos clic en el botón analizar, nos dirá las instancias que conoce y que desconoce. Las instancias desconocidas se guardarán en el archivo “unknownInstances.csv” ubicado en la carpeta de instalación del programa. 
![](http://i63.tinypic.com/f3b9sj.png)
4. Al finalizar nos mostrará una tabla con el identificador del evento, el resultado de la predicción y su probabilidad: 
![](http://i68.tinypic.com/14n1glh.jpg)
## Realizar el reentrenamiento del modelo:
1. Seguimos el mismo proceso para abrir un archivo csv, este archivo DEBE tener los campos que se listan a continuación:  Type, Transport Protocol, Aggregated Event Count, Priority, Agent Severity, Device Event Category, Device Action, Device Vendor, Attacker Address, Attacker Port, Attacker Geo Country Name, Target Address, Target Translated Address, Target Port, Target Service Name, Target Geo Location Info, Target Geo Country Name, Request Url, Device Custom String1, Device Custom String3, Device Custom String4, Illegitimate Request. Este último, campo debe ser diligenciado por el operador, siendo “Y” si es sospechoso y “N” si no es sospechoso. Ya que el modelo se va a reentrenar con el criterio del operador.
2. Hacemos clic en el botón reentrenar, y nos mostrará un mensaje de espera. 
![](http://i65.tinypic.com/124f1qw.png)
3. Al finalizar nos dará un resumen de los resultados del reentrenamiento del modelo con el número de instancias correctamente clasificadas, se debe tener en cuenta que este modelo es histórico.
![](http://i64.tinypic.com/2lbcapw.jpg)


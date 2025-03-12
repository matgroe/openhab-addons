# Gira One Binding
The [Gira One System](https://www.gira.com/en/en/products/systems/gira-one) offers a smart home solution on top of KNX 
without the need of using the Engineering Tool Software (ETS) for setting up your smart home system. 
Your [Smart Home Setup](https://partner.gira.com/data3/GiraOneSystemhandbuch_EN.pdf) will be done via the [Gira Projekt Assistent (GPA)](https://partner.gira.com/en/service/software-tools/gpa.html) 
and you're able to control your home by using the [Gira Smart Home App](https://partner.gira.com/en/service/apps.html?searchphrase=smart+home+app) which communicates 
with the Gira One Server through your IP network.

## Disclaimer
The whole communication between the openhab binding and the Gira One Server was reverse engineered by performing actions 
within the Smart Home App and analysing the network communication within the contributor's system. Therefore, there are 
some functional gaps and not every use cas is full covered.  

*We need your help to get the things better. Please let me know what's working well, what might be better and what's not 
working as expected.*

## Supported Things
This binding offers a bridge and let the things communicate with the gira one server via your local IP network.   

- `giraone:server`: The bridge between openhab and Gira One Smart Home. The Gira One Server must have a Firmware Version of _2.0.108.0_
- `giraone:status-humidity`: Gives information about a room's humidity. 
- `giraone:status-temperature`: Gives information about a room's temperature.
- `giraone:dimmer-light`: Controls Light On/Off and dimming with status information.
- `giraone:switch-lamp`: Switches a lamp On/Off and offers status information.
- `giraone:switch-power-outlet`: Switches a power outlet and offers status information.
- `giraone:shutter-venetian-blind`: Offers information about shutter position and let the shutter move up/down.
- `giraone:heating-cooling-underfloor`: Sets the temperature for your underfloor heater and gives some status information.
- `giraone:shutter-roof-window`: Offers information about roof window position and open/closes the window.
- `giraone:shutter-awning`: open/closes the awning.
- `giraone:function-scene`: executes a function scene as configured within the Gira Smart Home
  
## Discovery
The Gira One Server Binding is getting discovered within the local network via UPNP. After entering the credentials, 
all available things are getting discovered as well and will be sent to the _Things Inbox_. 

##  Binding Configuration 

It's only needed to configure the binding `giraone:server` itself. After the project setup is getting fetched from the gira one server after creating a connection.  

| Name                     | Type    | Description                                                                                                  | Default | Required | Advanced |
|--------------------------|---------|--------------------------------------------------------------------------------------------------------------|---------|----------|----------|
| hostname                 | text    | Hostname or IP address of the Gira One Server                                                                | N/A     | yes      | no       |
| username                 | text    | Username to access the device, defined by GPA                                                                | N/A     | yes      | no       |
| password                 | text    | Password to access the device, defined by GPA                                                                | N/A     | yes      | no       |
| tryReconnectAfterSeconds | integer | How many seconds should be waited before trying a reconnect on as serverside closed connection (e.g. reboot) | 60      | no       | yes      |
| defaultTimeoutSeconds    | integer | How long should be waited on any answer before failing?                                                      | 60      | no       | yes      |
| maxTextMessageSize       | integer | Maximum number of kBytes to accept from Gira One Server as single message.                                   | 350     | no       | yes      |

## Things

### _device_ Things

#### Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| control | Switch | RW         | This is the control channel |

## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

### Thing Configuration

```java
Example thing configuration goes here.
```
### Item Configuration

```java
Example item configuration goes here.
```

### Sitemap Configuration

```perl
Optional Sitemap configuration goes here.
Remove this section, if not needed.
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_




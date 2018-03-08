/**
 *  LF_ThremSW
 *
 *  Copyright 2018 Li Feng
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "TSwitch", namespace: "LF", author: "Li Feng") {
        attribute "status", "string"
   		capability "Polling"

		capability "Actuator"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Sensor"

		command "turnOnS1"
		command "turnOffS1"
		command "on_Z22"
		command "off_Z22"
		command "status"
	}


	preferences {
		input("DeviceIP", "string", title:"Device IP Address", description: "Please enter your device's IP Address", required: true, displayDuringSetup: true)
		input("DevicePort", "string", title:"Device Port", description: "Please enter port 80 or your device's Port", required: true, displayDuringSetup: true)
		input("DevicePath", "string", title:"URL Path", description: "Rest of the URL, include forward slash. like '/', /abc/", displayDuringSetup: true)
		input(name: "DevicePostGet", type: "enum", title: "POST or GET", options: ["POST","GET"], required: true, displayDuringSetup: true)
		input("DeviceBodyText", "string", title:'Body Content', description: 'Type in "GateTrigger=" or "CustomTrigger="', required: true, displayDuringSetup: true)
		input("UseJSON", "bool", title:"Use JSON instead of HTML?", description: "Use JSON instead of HTML?", defaultValue: false, required: false, displayDuringSetup: true)
		section() {
			input("HTTPAuth", "bool", title:"Requires User Auth?", description: "Choose if the HTTP requires basic authentication", defaultValue: false, required: true, displayDuringSetup: true)
			input("HTTPUser", "string", title:"HTTP User", description: "Enter your basic username", required: false, displayDuringSetup: true)
			input("HTTPPassword", "string", title:"HTTP Password", description: "Enter your basic password", required: false, displayDuringSetup: true)
		}
    }

	simulator {
	}

	tiles {
		standardTile("refresh", "command.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			//state "default", label:'', action:"polling.poll", icon:"st.secondary.refresh"
			state "default", label:'', action:"refresh", icon:"st.secondary.refresh"
		}

		standardTile("toggle1", "zONE1", width: 2, height: 2) {
			state("on", label:"Z 1\r\n" + "turn " + '${name}', action:"turnOnS1", icon:"", backgroundColor:"#00A0DC", nextState:"off")
			state("off", label:"Z 1\r\n" + "turn " + '${name}', action:"turnOffS1", icon:"", backgroundColor:"#7F0000", nextState:"on")
		}
		standardTile("on1", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Turn on Z1', action:"turnOnS1", icon:"st.switches.switch.on"
		}
		standardTile("off1", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Turn off Z1', action:"turnOffS1", icon:"st.switches.switch.off"
		}


		standardTile("toggle2", "gATE2", width: 2, height: 2) {
			state("on", label:"Z 2\r\n" + "turn " + '${name}', action:"openG2", icon:"", backgroundColor:"#00A0DC", nextState:"off")
			state("off", label:"Z 2\r\n" + "turn " + '${name}', action:"closeG2", icon:"", backgroundColor:"#7F0000", nextState:"on")
		}
		standardTile("on2", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Turn on Z2', action:"turnOnS2", icon:"st.doors.garage.garage-opening"
		}
		standardTile("off2", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Turn off Z2', action:"turnOffS2", icon:"st.doors.garage.garage-closing"
		}
        
		main (["toggle1", "toggle2"])
		details(["toggle1", "on1", "off1", "toggle2", "on2", "off2", "refresh"])
	}
    
}

def initialize() {
    state.date = new Date()
    state.refreshCount = 10
	log.debug "----------" + state.date
	log.debug "----------------init called, refreshCount = " + state.refreshCount
}

def updated() {
	log.debug "----------------update called"
    initialize()
	unschedule()
	//runEvery1Minute(refresh)
	//runEvery5Minutes(refresh)
	runIn(2, refresh)	//after 2 second take a run for refresh()
}
def poll() {
	log.debug "Executing 'poll'"
	log.debug "do nothing on 'poll'"
	//status()
	//parent.poll()
}
def refresh() {
	log.debug "---------------refresh"
    status();
}

def onoffGate(int gid) {
	log.debug "Toggle gate $gid"
    if (gid == -1) {
    } else {
    	runCmd("open?gid=${gid}")
    }
	//runIn(7, refresh)	//after 12 second take a run for refresh()
    //schedule(now() + 13000, refresh)
}

def turnOnAll() {
	runCmd("open?gid=-1")
}

def turnOffAll() {
	runCmd("close?gid=-1")
}

def turnOnS1() {
	runCmd("open?gid=1")
}

def turnOffS1() {
	runCmd("close?gid=1")
}

def turnOnS2() {
	runCmd("open?gid=2")
}
def turnOffS2() {
	runCmd("clolse?gid=2")
}

def status() {
	log.debug "get status"
	sendEvent(name: "toggle1", value: "trying", isStateChange: true)
	sendEvent(name: "toggle2", value: "trying", isStateChange: true)
	runCmd("status")	
}

def runCmd(String varCommand) {
	def host = DeviceIP
    def hosthex = convertIPtoHex(host).toUpperCase()
	def porthex = convertPortToHex(DevicePort).toUpperCase()
	device.deviceNetworkId = "$hosthex:$porthex"
	def userpassascii = "${HTTPUser}:${HTTPPassword}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()

	//log.debug "The device id configured is: $device.deviceNetworkId"

	//def path = DevicePath
	def path = DevicePath + varCommand
	log.debug "path is: $path"
	//log.debug "Uses which method: $DevicePostGet"
	def body = ""//varCommand
	log.debug "body is: $body"

	def headers = [:]
	headers.put("HOST", "$host:$DevicePort")
	headers.put("Content-Type", "application/x-www-form-urlencoded")
	if (HTTPAuth) {
		headers.put("Authorization", userpass)
	}
	log.debug "The Header is $headers"
	def method = "POST"
	try {
		if (DevicePostGet.toUpperCase() == "GET") {
			method = "GET"
			}
		}
	catch (Exception e) {
		settings.DevicePostGet = "POST"
		log.debug e
		log.debug "You must not have set the preference for the DevicePOSTGET option"
	}
	log.debug "The method is $method"
	try {
		def hubAction = new physicalgraph.device.HubAction(
			method: method,
			path: path,
			body: body,
			headers: headers
			)
		hubAction.options = [outputMsgToS3:false]
		//log.debug hubAction
		hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
}

def parse(String description) {
	//log.debug "Parsing '${description}'"
	def whichTile = ''	
	//log.debug "state.blinds " + state.blinds
    
    def msg = parseLanMessage(description)
	def headersAsString = msg.header // => headers as a string
	def headerMap = msg.headers      // => headers as a Map
	def body = msg.body              // => request body as a string
	def status = msg.status          // => http status code of the response
	def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
	def xml = msg.xml                // => any XML included in response body, as a document tree structure
	def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)
	log.debug "Receiving Message From Device: " + status + " body: " + body
	log.debug "JSON: " + json

	int sw_state
	if (json.containsKey("states")){
		log.debug "sw state: " + json.states
    	sw_state = json.states
	} else {
    	sw_state = 0
    }
	if (json.containsKey("temperature")){
		log.debug "temperature: " + json.temperature
	}
    
    def sw1 = doorStatus((sw_state)&0x01)
	log.debug "sw_1: " + sw1

    def sw2 = doorStatus((sw_state)&0x02)
	log.debug "sw_2: " + sw2
    
    int state1 = (sw_state)&0x01
    int state2 = (sw_state)&0x02
    
    switch(state1) {
    	case 0:	//off
			sendEvent(name: "gATE1", value: "off", isStateChange: true)
			//def result = createEvent(name: "switch", value: "off", isStateChange: true)
			//return result
        	break
    	case 1:	//on
			sendEvent(name: "gATE1", value: "on", isStateChange: true)
        	break
    }

    switch(state2) {
    	case 0:	//off
			sendEvent(name: "gATE2", value: "off", isStateChange: true)
			//def result = createEvent(name: "switch", value: "off", isStateChange: true)
			//return result
        	break
    	case 2:	//on
			sendEvent(name: "gATE2", value: "on", isStateChange: true)
        	break
    }

}

private String convertIPtoHex(ipAddress) {
	String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	//log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
	return hex
}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
	//log.debug hexport
	return hexport
}
private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}
private String convertHexToIP(hex) {
	//log.debug("Convert hex to ip: $hex")
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	//log.debug device.deviceNetworkId
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

private String doorStatus(int ocValue) {
  String state;
  switch (ocValue) {
    case 0:
      state = "off";
      break;
    case 1:
      state = "on";
      break;
    default:
      state = "something wrong!";
      break;
  }
  return state;
}
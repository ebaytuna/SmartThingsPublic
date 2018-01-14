/**
 *  Generic HTTP Device v1.0.20160402
 *
 *  Source code can be found here: https://github.com/LF-SmartThings/
 *  Copyright 2018 LF
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

import groovy.json.JsonSlurper

metadata {
	definition (name: "LF-ESP8266_HTTP_Device", author: "lf", namespace:"") {
		capability "Switch"
        attribute "status", "string"
		command "status"
		attribute "triggerswitch", "string"
		command "DeviceTrigger"
		capability "Sensor"
   		capability "Polling"

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
		standardTile("DeviceTrigger", "device.triggerswitch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "triggeroff", label:'CLOSED' , action: "on", backgroundColor:"#ffffff", nextState: "trying"
			state "triggeron", label: 'OPEN', action: "off", backgroundColor: "#79b821", nextState: "trying"
			state "trying", label: 'TRYING', action: "", backgroundColor: "#FFAA33"
		}
        
		valueTile("temperature", "device.temperature", width: 1, height: 1) {
			state("temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}

		standardTile("refresh", "command.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"polling.poll", icon:"st.secondary.refresh"
		}

		main (["DeviceTrigger", "temperature"])
		details (["DeviceTrigger", "temperature", "refresh"])
		//details(["DeviceTrigger", "oscTrigger", "modeTrigger", "speedTrigger", "timerAddTrigger", "timerMinusTrigger"])
	}

}

def on() {
	log.debug "Triggered OPEN!!!"
	//sendEvent(name: "triggerswitch", value: "triggeron", isStateChange: true)
    state.blinds = "on";
	runCmd("open")
}
def off() {
	log.debug "Triggered CLOSE!!!"
	//sendEvent(name: "triggerswitch", value: "triggeroff", isStateChange: true)
    state.blinds = "off";
	runCmd("close")
}
def status() {
	log.debug "get status"
	sendEvent(name: "triggerswitch", value: "trying", isStateChange: true)
	runCmd("status")	
}
def poll() {
	log.debug "poll"
	log.debug "Executing 'poll'"
	status()
	//parent.poll()
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
	//log.debug "The Header is $headers"
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

	if (json.containsKey("state")){
		log.debug "switch status: " + json.state
	}

	
    //if (state.blinds == "on") {
    if (json.containsKey("state") && json.state == 1) {
    	//sendEvent(name: "triggerswitch", value: "triggergon", isStateChange: true)
        whichTile = 'mainon'
		//log.debug "1status: " + json.state
    }
    //if (state.blinds == "off") {
    if (json.containsKey("state") && json.state == 0) {
    	//sendEvent(name: "triggerswitch", value: "triggergoff", isStateChange: true)
        whichTile = 'mainoff'
		//log.debug "0status: " + json.state
    }
    
    if (json.containsKey("temperature")) {
    	sendEvent(name: "temperature", value: json.temperature, isStateChange: true)
        //whichTile = 'temper'
		log.debug "temperature: " + json.temperature
    }
	
    //RETURN BUTTONS TO CORRECT STATE
	//log.debug 'whichTile: ' + whichTile
    switch (whichTile) {
        case 'mainon':
			sendEvent(name: "triggerswitch", value: "triggeron", isStateChange: true)
			def result = createEvent(name: "switch", value: "on", isStateChange: true)
			return result
        case 'mainoff':
			sendEvent(name: "triggerswitch", value: "triggeroff", isStateChange: true)
			def result = createEvent(name: "switch", value: "off", isStateChange: true)
			return result
        default:
			def result = createEvent(name: "testswitch", value: "default", isStateChange: true)
			log.debug "testswitch returned ${result?.descriptionText}"
			return result
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
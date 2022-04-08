
default:
	cd testRunner
	gradlew build
	java -jar build/libs/testRunner-1.0-SNAPSHOT.jar configurations/example_config.json $(VERSION)
	cd ..
	cd UI
	python graph.py
	
	
test:
	
clean:
	cls
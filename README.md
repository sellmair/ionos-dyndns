### IONOS DNYDNS

___
_Disclaimer_<br>
_This project showcases Kotlin's multiplatform capabilities for command line applications. 
It is built with Kotlin/Native for linuxX64 and macosX64 as well as for the JVM<br>
Note: Right now, the native binaries are relying on the -native-mt branch of coroutines and are therefore
leaking memory_
___

### Usage
#### Configuration
The service binary is looking for a configuraiton file in ~/.ionos-dyndns/config.json

Sample:

```json
{
  "ipRefreshInterval": "1m",
  "domains": [
    {
      "apiKey": "someApiKeyThatIProbablyWontPushIntoAnyRepo",
      "rootDomainName": "sellmair.io",
      "targetDomainName": "sellmair.io",
      "timeToLive": "5m",
      "ipProvider": "amazon"
    }, 
    {
      "apiKey": "someApiKeyThatIProbablyWontPushIntoAnyRepo",
      "rootDomainName": "sellmair.io",
      "targetDomainName": "subdomain.sellmair.io",
      "timeToLive": "5m",
      "ipProvider": "amazon"
    }, 
    {
      "apiKey": "someApiKeyThatIProbablyWontPushIntoAnyRepo",
      "rootDomainName": "sellmair.io",
      "targetDomainName": "anotherdomain.sellmair.io",
      "timeToLive": "5m",
      "ipProvider": "amazon"
    }
  ]
}

```

###### ipRefreshInterval
Time between the application shall re-check if the external current IP address changed
- `1m`: Check current IP address every minutes
- `1.5h`: Check current IP address every 1.5 hours
- `0.33d`: Check current IP address 3 times a day

###### domains
List of domains to update

###### apiKey
The ionos api key (see: https://developer.hosting.ionos.de/docs/getstarted#support) associated with the domain

###### rootDomainName
The name of the domain that you want to update (without subdomain)

###### targetDomainName
The name of the domain (with or without subdomain) that should be udpated

###### timeToLive
Corresponds to the "ttl" of the dns record
- `1m`: Records can be cached for one minute
- `1.5h`: Records can be cached for 1 hour 30 minutes
- ...

###### ipProvider
The preffered method of querying the current external IP address. 
- "amazon"  Uses "https://checkip.amazonaws.com/" to find IP
- "url:https://my.ip-service.com" will use specified URL to qury IP address

Bootstrap Migration Tips
========================

* `p:panel` -> `ui:fragment`, or just the content, when possible.
* Whenever possible, no xhtml at all
	* `<h:outputText value="abc"/>` -> abc
* conditional rendering based on `ui:fragment`'s `rendered` property.
* Cant use HTML entities. Use the actual unicode char instead.
* HTML5's attributes using passthrough:
	
	h:inputText id="email" value="#{bean.email}">
	<f:passThroughAttribute name="type" value="email"/>
	<f:passThroughAttribute name="placeholder"
		value="Enter email"/>
	</h:inputText>

* More on html5 and JSF 2.2 at http://jsflive.wordpress.com/2013/08/08/jsf22-html5/
* We have a bootstrap component lib, `iqbs`.

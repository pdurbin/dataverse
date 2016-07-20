#!/usr/bin/env python
from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from time import sleep
driver = webdriver.Firefox()
baseurl = "http://localhost:8080"
driver.get(baseurl + "/loginpage.xhtml?redirectPage=%2Fdataverse.xhtml")
elem = driver.find_element_by_id("loginForm:credentialsContainer2:0:credValue")
#elem.send_keys("dataverseAdmin")
elem.send_keys("wren")
num_attempts = 4
for i in range(num_attempts):
    password_input_id = "loginForm:credentialsContainer2:1:sCredValue"
    elem = driver.find_element_by_id(password_input_id)
    elem.send_keys(i)
    elem.send_keys(Keys.RETURN)
    sleep(1)

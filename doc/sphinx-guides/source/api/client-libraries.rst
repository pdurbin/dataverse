Client Libraries
================

Currently there are client libraries for Python, Javascript, R, and Java that can be used to develop against Dataverse APIs. We use the term "client library" on this page but "Dataverse SDK" (software development kit) is another way of describing these resources. They are designed to help developers express Dataverse concepts more easily in the languages listed below. For support on any of these client libraries, please consult each project's README.

Because Dataverse is a SWORD server, additional client libraries exist for Java, Ruby, and PHP per the :doc:`/api/sword` page.

.. contents:: |toctitle|
	:local:

Python
------

There are two Python modules for interacting with Dataverse APIs.

`pyDataverse <https://github.com/AUSSDA/pyDataverse>`_ had its initial release in 2019 and can be installed with ``pip install pyDataverse``. The module is developed by `Stefan Kasberger <http://stefankasberger.at>`_ from `AUSSDA - The Austrian Social Science Data Archive <https://aussda.at>`_.  

`dataverse-client-python <https://github.com/IQSS/dataverse-client-python>`_ had its initial release in 2015. `Robert Liebowitz <https://github.com/rliebz>`_ created this library while at the `Center for Open Science (COS) <https://centerforopenscience.org>`_ and the COS uses it to integrate the `Open Science Framework (OSF) <https://osf.io>`_ with Dataverse via an add-on which itself is open source and listed on the :doc:`/api/apps` page.

Javascript
----------

https://github.com/IQSS/dataverse-client-javascript is the official Javascript package for Dataverse APIs. It can be found on npm at https://www.npmjs.com/package/js-dataverse

It was created and is maintained by `The Agile Monkeys <https://www.theagilemonkeys.com>`_.

R
-

https://github.com/IQSS/dataverse-client-r is the official R package for Dataverse APIs. The latest release can be installed from `CRAN <https://cran.r-project.org/package=dataverse>`_.

It was created by `Thomas Leeper <http://thomasleeper.com>`_ whose dataverse can be found at https://dataverse.harvard.edu/dataverse/leeper

Java
----

https://github.com/IQSS/dataverse-client-java is the official Java library for Dataverse APIs.

`Richard Adams <http://www.researchspace.com/electronic-lab-notebook/about_us_team.html>`_ from `ResearchSpace <http://www.researchspace.com>`_ created and maintains this library.

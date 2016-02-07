#Description
This module helps securing access to the dyn/admin console on ATG instances.

#Module Deployment

You have two options to deploy this code:

- Option 1: Embbed the module in your project code and specify which ones to start (This is the preferred way).

- Option 2:
    + make a config.jar from the config folder and deploy it using the ATG groupconfig mechanism.
    + make a classes.jar from the classes folder which is embedded in the module and deploy it as a module (for JBOSS), endorsed libraries etc... The requirements is that the JAR should be in your server's classpath when you start it.

#Security Configuration

##Initializing the accounts

The AdminAccountInitializer service is an out of the box ATG service which populates the dyn/admin accounts and user groups included in the **/atg/dynamo/security/admin-accounts.xml** file.

This file follows the same XML combination rules as any XML files in ATG  so you can override or append data to its content just like when you extend the repository definitions.

To reinitialize the administrator accounts, you need to :

1. stop ATG
2. delete the content of the **das_account** table
3. start ATG

The data from the admin-accounts.xml file will be loaded automatically into the admin repository.

##Basic Security

By default, the security modules uses the **administrators-group** and the **developpers-group** groups to filter user requests.

The default ATG **admin** user is part of the **administrators-group** and has all rights on all pages.

The users in **developpers-group** have restricted rights:

- They have the right to browse components and check the property file combinations only.
- They can query repositories but can not change items in them.
- All other pages are forbidden.

##Configuring the Security Services

The **AdminPathSecurityServlet** pipeline servlet enables you to map URL substrings with a **RequestCheckerService**.

The module comes with an simple implementation of **RequestCheckerService**, **ActionRequestChecker** which has the following properties. To understand how to configure this service, take a look at the javadoc.

The security module comes with two pre-configured request checker services:

- The **SuperUserRequestChecker** which allows admin users only to navigate the dyn/admin pages
- The **NucleusRequestChecker** which allows users to browse the Nucleus components with restricted rights (pretty much a read only right).

##Security Audit Log

Although ATG 11 has a new security audit log, our module logs detailed information on who requested what and from what IP so you can track all actions made through the dyn/admin.

This file is create under the ```${DYNAMO_HOME}/servers/<your server>/logs``` folder and is called **admin.log**.

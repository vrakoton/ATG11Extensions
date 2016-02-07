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

##Configuring the security services
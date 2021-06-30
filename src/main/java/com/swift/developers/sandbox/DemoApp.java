package com.swift.developers.sandbox;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.*;
import com.google.gson.internal.$Gson$Preconditions;
import com.swift.commons.context.Context;
import com.swift.commons.context.KeyStoreContext;
import com.swift.commons.exceptions.SignatureContextException;
import com.swift.commons.token.ChannelToken;
import com.swift.commons.token.Token;
import com.swift.developers.sandbox.exception.ApiSessionException;
import com.swift.developers.sandbox.session.impl.SandboxApiSession;
import com.swift.developers.sandbox.util.Constants;
import com.swift.developers.sandbox.util.Util;
import com.swift.sdk.oas.gpi.tracker.v3.model.CancelTransactionRequest;
import com.swift.sdk.oas.gpi.tracker.v3.model.TransactionCancellationStatusRequest;
import com.swift.sdk.oas.gpi.tracker.v4.ApiClient;
import com.swift.sdk.oas.gpi.tracker.v4.ApiException;

import com.swift.sdk.oas.gpi.tracker.v4.api.CancelTransactionApi;
import com.swift.sdk.oas.gpi.tracker.v4.api.GetChangedPaymentTransactionsApi;
import com.swift.sdk.oas.gpi.tracker.v4.api.GetPaymentTransactionDetailsApi;
import com.swift.sdk.oas.gpi.tracker.v4.api.StatusConfirmationsApi;
import com.swift.sdk.oas.gpi.tracker.v4.api.TransactionCancellationStatusApi;

import com.swift.sdk.oas.gpi.tracker.v4.model.*;
import com.swift.sdk.gpitracker.v4.util.GpiTrackerUtil;

import com.swift.sdk.util.ConnectionInfo;
import com.swift.sdk.util.GPIUtil;
import org.apache.commons.lang.StringUtils;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.Test;
import org.threeten.bp.OffsetDateTime;

import java.io.BufferedOutputStream;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static com.swift.sdk.util.Constants.ENC_CONFIG_FILEPATH;
import static com.swift.sdk.util.Constants.ENC_SECRETS_FILEPATH;
import static com.swift.sdk.util.GPIConstants.CANCEL_TRANSACTION_SERVICE_V4;


public class DemoApp {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage : DemoApp <Configuration File Name>");
            System.exit(-1);
        } else {
            System.out.println("Using the configuration file - " + args[0] + " to setup the session.");
        }
        try {
            SandboxApiSession sess = new SandboxApiSession(args[0], Util.CertType.SOFT);
            System.out.println("\nSession is established successfully.");
            System.out.println("Access Token - " + sess.getAccessToken());
            System.out.println("Access Token Expiry - " + sess.getTokenExpiry());
            System.out.println("Refresh Token - " + sess.getRefreshToken());
            System.out.println("Refresh Token Expiry - " + sess.getRefreshExpiry() + "\n");

            String number = "";
            int num = 0;
            Scanner scan = new Scanner(System.in);

            do {
                scan = new Scanner(System.in);
                System.out.print("\n--------------Select the API you would like to call-------------------\n");
                System.out.print("1 - StatusConfirmation\n" + "2 - getPaymentTransactionDetails\n" + "3 - getChangedPaymentTransaction\n" + "4 - CancelTransaction\n" + "5 - TransactionCancellationStatus\n" + "\nSelect an API you would like to call or 'bye' to exit: ");
                number = scan.nextLine();

                if (!number.equalsIgnoreCase("")) {
                    if (StringUtils.isNumeric(number)) {
                        num = Integer.parseInt(number);
                        if (num == 1) {
                            StatusConfirmation(sess);
                        } else if (num == 2) {
                            getPaymentTransactionDetails(sess);
                        } else if (num == 3) {
                            getChangedPaymentTransaction(sess);
                        } else if (num == 4) {
                            CancelTransaction(sess);
                        } else if (num == 5) {
                            TransactionCancellationStatus(sess);
                        }
                    }
                }
            } while (number.equalsIgnoreCase("") || !number.equalsIgnoreCase("bye"));

            scan.close();

        } catch (ApiSessionException ex) {
            // TODO Auto-generated catch block
        	System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void StatusConfirmation(SandboxApiSession sess) {
        try {
            StatusConfirmationsApi cgdapi = new StatusConfirmationsApi();
            CamtA0100105 body = new CamtA0100105();
            RestrictedFINActiveOrHistoricCurrencyAndAmount restrictedFINActiveOrHistoricCurrencyAndAmount = new RestrictedFINActiveOrHistoricCurrencyAndAmount();

            String uetr = "d2ecb184-b622-41e9-a2a3-2a2ae2dbcce4";

            body.setFrom("BANCUS33XXX");
            body.setBusinessService(BusinessService6Code._001);
            body.setUpdatePaymentScenario(PaymentScenario5Code.COVE);
            body.setInstructionIdentification("jkl000");
            body.setOriginator("BANCUS33XXX");
            body.setFundsAvailable("2020-08-30T17:00:00.0Z");
            body.setTransactionStatus(TransactionIndividualStatus5Code.ACCC);
            body.setReturn(Boolean.FALSE);
            body.setConfirmedAmount(restrictedFINActiveOrHistoricCurrencyAndAmount.currency("EUR"));
            body.setConfirmedAmount(restrictedFINActiveOrHistoricCurrencyAndAmount.amount("970"));

            String basepath = sess.getBasePath(Constants.GATEWAY_HOST, Constants.TRACKER_SERVICE_V4);
            cgdapi.setApiClient((ApiClient) sess.prepareApiClient(cgdapi.getApiClient(), basepath));
            cgdapi.statusConfirmations(body, uetr);

            String url = "\nURL: https://sandbox.swift.com/swift-apitracker/v4/payments/" + uetr + "/status\n";
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(body);
            String response = "\n200 OK";

            System.out.println("\nREQUEST" + url + "\nBody:\n" + jsonOutput + "\n" + "\nRESPONSE" + response);

        } catch (ApiException ex) {
            System.out.println("\nERROR - " + ex.getMessage());
            ex.printStackTrace();
            System.out.println(ex.getResponseBody());           
        } catch (Exception ex) {
        	System.out.println("ERROR - " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void getPaymentTransactionDetails(SandboxApiSession sess) {
        try {

            CamtA0200205 response = null;
            GetPaymentTransactionDetailsApi cgdapi = new GetPaymentTransactionDetailsApi();

            String uetr = "97ed4827-7b6f-4491-a06f-b548d5a7512d";
            String basepath = sess.getBasePath(Constants.GATEWAY_HOST, Constants.TRACKER_SERVICE_V4);
            cgdapi.setApiClient((ApiClient) sess.prepareApiClient(cgdapi.getApiClient(), basepath));
            response = cgdapi.getPaymentTransactionDetails(uetr);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(response);

            String url = "\nURL: https://sandbox.swift.com/swift-apitracker/v4/payments/" + uetr + "/transactions\n";

            System.out.println("\nREQUEST" + url + "\nRESPONSE\n " + jsonOutput);

        } catch (ApiException ex) {
            System.out.println("ERROR - " + ex.getMessage());
            ex.printStackTrace();
            System.out.println(ex.getResponseBody());            
        } catch (Exception ex) {
        	System.out.println("ERROR - " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void getChangedPaymentTransaction(SandboxApiSession sess) {
        try {
            CamtA0400205 response = null;
            GetChangedPaymentTransactionsApi cgdapi = new GetChangedPaymentTransactionsApi();

            OffsetDateTime fromDateTime = OffsetDateTime.parse("2020-04-11T00:00:00.0Z");
            OffsetDateTime toDateTime = OffsetDateTime.parse("2020-04-16T00:00:00.0Z");
            int maxNumber = Integer.parseInt("10");
            String paymentScenario = "CCTR";
            String next = null;
            String basepath = sess.getBasePath(Constants.GATEWAY_HOST, Constants.TRACKER_SERVICE_V4);
            cgdapi.setApiClient((ApiClient) sess.prepareApiClient(cgdapi.getApiClient(), basepath));
            response = cgdapi.getChangedPaymentTransactions(fromDateTime, toDateTime, maxNumber, paymentScenario, next);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(response);

            String url = "\nURL: https://sandbox.swift.com/swift-apitracker/v4/payments/changed/transactions?from_date_time=" + fromDateTime + "&to_date_time=" + toDateTime + "&maximum_number=" + maxNumber + "\n";

            System.out.println("\nREQUEST" + url + "\nRESPONSE\n " + jsonOutput);

        } catch (ApiException ex) {
            System.out.println("ERROR - " + ex.getMessage());
            ex.printStackTrace();
            System.out.println(ex.getResponseBody());            
        } catch (Exception ex) {
        	System.out.println("ERROR - " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void CancelTransaction(SandboxApiSession sess) {
        try {
            CamtA0600104 body = new CamtA0600104();
            CancelTransactionApi cgdapi = new CancelTransactionApi();

            String uetr = "97ed4827-7b6f-4491-a06f-b548d5a7512d";

            body.setFrom("BANABEBBXXX");
            body.setBusinessService(BusinessService2Code._002);
            body.setCaseIdentification("123");
            body.setOriginalInstructionIdentification("XYZ");
            body.setCancellationReasonInformation(CancellationReason8Code.DUPL);
            body.setIndemnityAgreement(PendingPaymentCancellationReason2Code.INDM);

            Map<String, Object> claimsMap = new HashMap<>();
            ConnectionInfo conInfo = GpiTrackerUtil.createConnectionInfo(sess.getConfigJson());
            claimsMap.put("audience", GpiTrackerUtil.buildAbsServicePath(conInfo, sess.getConfigJson(), CANCEL_TRANSACTION_SERVICE_V4, uetr));
            claimsMap.put("payload", body);
            Token token = new ChannelToken();
            Context context = new KeyStoreContext(conInfo.getCertPath(), conInfo.getCertPassword(), 
            											conInfo.getCertPassword(), conInfo.getCertAlias());           
            String xSWIFTSignature = token.createNRSignature(context, claimsMap);

            String basepath = sess.getBasePath(Constants.GATEWAY_HOST, Constants.TRACKER_SERVICE_V4);
            cgdapi.setApiClient((ApiClient) sess.prepareApiClient(cgdapi.getApiClient(), basepath));
            cgdapi.cancelTransaction(body, xSWIFTSignature, uetr);

            String url = "\nURL: https://sandbox.swift.com/swift-apitracker/v4/payments/" + uetr + "/cancellation\n";
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(body);
            String response = "\n200 OK";

            System.out.println("\nREQUEST" + url + "\nHeader Parameters:\n" + "X-SWIFT-Signature: " + xSWIFTSignature + "\n" + "\nBody:\n" + jsonOutput + "\n" + "\nRESPONSE" + response);

        } catch (ApiException ex) {
            System.out.println("ERROR - " + ex.getMessage());
            ex.printStackTrace();
            System.out.println(ex.getResponseBody());           
        } catch (Exception ex) {
        	System.out.println("ERROR - " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void TransactionCancellationStatus(SandboxApiSession sess) {
        try {
            CamtA0700104 body = new CamtA0700104();
            TransactionCancellationStatusApi cgdapi = new TransactionCancellationStatusApi();

            String uetr = "97ed4827-7b6f-4491-a06f-b548d5a7512d";

            body.setFrom("BANBUS33XXX");
            body.setBusinessService(BusinessService2Code._002);
            body.setAssignmentIdentification("resolvedcase123");
            body.setCaseIdentification("123");
            body.setInvestigationExecutionStatus(InvestigationExecutionConfirmation5Code.CNCL);
            body.setOriginator("BANUS33XX");

            Map<String, Object> claimsMap = new HashMap<>();
            ConnectionInfo conInfo = GpiTrackerUtil.createConnectionInfo(sess.getConfigJson());
            claimsMap.put("audience", GpiTrackerUtil.buildAbsServicePath(conInfo, sess.getConfigJson(), CANCEL_TRANSACTION_SERVICE_V4, uetr));
            claimsMap.put("payload", body);
            Token token = new ChannelToken();
            Context context = new KeyStoreContext(conInfo.getCertPath(), conInfo.getCertPassword(), 
            											conInfo.getCertPassword(), conInfo.getCertAlias());
            String xSWIFTSignature = token.createNRSignature(context, claimsMap);

            String basepath = sess.getBasePath(Constants.GATEWAY_HOST, Constants.TRACKER_SERVICE_V4);
            cgdapi.setApiClient((ApiClient) sess.prepareApiClient(cgdapi.getApiClient(), basepath));
            cgdapi.transactionCancellationStatus(body, xSWIFTSignature, uetr);

            String url = "\nURL: https://sandbox.swift.com/swift-apitracker/v4/payments/" + uetr + "/cancellation/status\n";
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(body);
            String response = "\n200 OK";

            System.out.println("\nREQUEST" + url + "\nHeader Parameters:\n" + "X-SWIFT-Signature: " + xSWIFTSignature + "\n" + "\nBody:\n" + jsonOutput + "\n" + "\nRESPONSE" + response);

        } catch (ApiException ex) {
            System.out.println("\nERROR - " + ex.getMessage());
            ex.printStackTrace();
            System.out.println(ex.getResponseBody());
        } catch (Exception ex) {
        	System.out.println("ERROR - " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

/*
 * Copyright 2012 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.eureka;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Sample Eureka client that discovers the example service using Eureka and sends requests.
 *
 * In this example, the program tries to get the example from the EurekaClient, and then
 * makes a REST call to a supported service endpoint
 *
 */
public class ExampleEurekaClient {

    private static ApplicationInfoManager applicationInfoManager;
    private static EurekaClient eurekaClient;

    private static synchronized ApplicationInfoManager initializeApplicationInfoManager(EurekaInstanceConfig instanceConfig) {
        if (applicationInfoManager == null) {
            // 根据eurekaInstanceConfig创建instanceInfo
            InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
            applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);
        }

        return applicationInfoManager;
    }

    private static synchronized EurekaClient initializeEurekaClient(ApplicationInfoManager applicationInfoManager, EurekaClientConfig clientConfig) {
        if (eurekaClient == null) {
            eurekaClient = new DiscoveryClient(applicationInfoManager, clientConfig);
        }

        return eurekaClient;
    }

    /**
     * This will be read by server internal discovery client. We need to salience it.
     */
    private static void injectEurekaConfiguration() throws UnknownHostException {
        String myHostName = InetAddress.getLocalHost().getHostName();
        String myServiceUrl = "http://" + myHostName + ":8080/v2/";

        System.setProperty("eureka.region", "default");
        System.setProperty("eureka.name", "eureka");
        System.setProperty("eureka.vipAddress", "eureka.mydomain.net");
        System.setProperty("eureka.port", "8080");
        System.setProperty("eureka.preferSameZone", "false");
        System.setProperty("eureka.shouldUseDns", "false");
        System.setProperty("eureka.shouldFetchRegistry", "false");
        System.setProperty("eureka.serviceUrl.defaultZone", myServiceUrl);
        System.setProperty("eureka.serviceUrl.default.defaultZone", myServiceUrl);
        System.setProperty("eureka.awsAccessId", "fake_aws_access_id");
        System.setProperty("eureka.awsSecretKey", "fake_aws_secret_key");
        System.setProperty("eureka.numberRegistrySyncRetries", "0");
    }

    public static void main(String[] args) throws UnknownHostException {
        injectEurekaConfiguration();

        ExampleEurekaClient sampleClient = new ExampleEurekaClient();

        // create the client
        // 1. 创建ApplicationInfoManager, 管理(读出来的eureka-client配置)instanceInfo
        ApplicationInfoManager applicationInfoManager = initializeApplicationInfoManager(new MyDataCenterInstanceConfig());
        // 2. 創建EurekaClientConfig(从eureka-client里读), 根据config和applicationInfo(instanceInfo 要被注册服务信息)
        //      创建一个client(普通的DiscoveryClient), 还没连接
        EurekaClient client = initializeEurekaClient(applicationInfoManager, new DefaultEurekaClientConfig());

        // 3. 使用client查一个eureka-server的instanceInfo, 然后和人家连起来了.
        sampleClient.sendRequestToServiceUsingEureka(client);


        // shutdown the client
        eurekaClient.shutdown();


    }


    /*
     *  看我画的流程图: https://online.visual-paradigm.com/w/zvrkggrh/app/diagrams/?title=Eureka_client%E5%90%AF%E5%8A%A8%E8%BF%9E%E6%8E%A5-%E5%8F%91%E9%80%81instanceInfo#R3cU2FsdYGVkX1c%2BcA4Cz5FYDxgxZ5P3E1I1tVL%2Fz7F4p%2Beba4ZtsQ%3Du8kOVZL5W4cJBleZnuwSirqZZD0i9yvJu125VvUz3WYofb14dY7qLN5XmHCf3cuIXf4GW9t7KYDov0B99qIdDl4NzXbxXglrQVW%2BNTOMJj7i68sI06ldoLdvc4EMOADA95StAAJYK%2FlNbO9H1kxl8jOv%2BSViE89jAgpxWY71r3E6CQNLGdHe9KYbb0knKr60WQUSrfTbJnX7DnZ%2F4k6wrlF7zN799P5x3dOHd7CYLlqgZjAPHNzko9bMEWkPwDkkIsRxlOp3IWEXTP%2F6g1PRW9RzmsOtjtvrFh7HeAWVkl%2FJk0SP4f8qH0zbCmxB0vNsNANduiISaSNFzOR7cjjP9%2FLBSK63Y4PnHj3vnlNJ69%2B1tc01hDlyPcCXZtcSM3LQU8fEf%2FaWk58FuIgft5%2BKmm5OiJhqceomSQ9l7lBvSJ%2BaZP3PI%2BNy9FNtzLg2%2FZnXVkmoMaVt7C5S8e2j4ZGnlYvo4iCRxvMbaLJYg2gqPoezTlXe%2FX79KEO2l9uf8Xi8jlqquoorHA9fKUvS4akOHjgPs2UwYniUgytLXtd%2BZyaOUeQhv3JrWp2aufnV9AQGE9IiP6r5TVJ1%2FwHDELOtlobLDzeDMcgMIPFKYw39y%2FqoZTlb8VpurTzUgRcpa6xk8TTFLwd73Zyja8AUhwThk0hX51%2BAs1yktBYLVp2dXn7c%2Bk4h9LeuFYxm6Ys24PhxSxhIKEr8NYwtXjmI6UYLBdIMLs%2BU6OdZ3N8v9n%2Br9kl4Mwldz%2B8vrzZQNbh%2FXmuefqkRdJUC6hD4WcqyoyKVNckzwh1pYbYi9kNeG%2FzrmvpC9WaPjJKwvCwLrLG9S4VLNCs%2BzaiHDvCfOEDkGf8FvHMktdrYx0JBXV6FZkp5uknizJ2%2FL%2B3DflyJwmNUETQwv6i1hbft%2B4lX8lxjO6Ft7PAYvTy%2FyHBwN0Vk4h%2FjlbQ2Scc0BqxN2QblK5fnv9B6MOkQv6Zbaf6kNLBQn9LFIOG6dPqWkGCUeExrlpONcEthb1zFh1uooX9C1ws2BYlmht8%2FLXxKzrllRapc3Zf2sVgHQFYQzZg5TkmxyAqaUHG%2BtGfyMMuCwRPivaWNP5PGJH7ky39p1jLGmD%2BEAFjjJD6OJdpMFdo%2F%2Fs3zVOlksXTo2qT68bHtuBiPwXMydLbHfWMUZ7uLGp1bspcDH3yIywLu%2F1Mlz0a0w7By4cLa9CJueXTDXAtpe8jOUAzt4GF%2F9NhgeRMLCw6Kkvv2wfmel0SXmJMu61suh9IPS2iUAmEKjNNkzfbnFOV407VgROZnmG14HazeXSl7Bz9LKZagdfMc5SFqKVSUnxthsvYYyANHVYLrCzlWBy%2FGxE4zlERVFsXNFr366vluNgPt7xAhj6GOCa8nXiU7Mhzae%2F5BbIEjR0HuffmGaYduiYvAwItesgzliLjtEq0cp2GPQAB2gokXhCCPP9yckm5NSweCDVmHTADwlHmJESf6hJSEFcYdMUXR3hxgmfahAYQoWUsJUAva5ZD3QL3%2FSO3%2FxFqJSI%2FiJwc3UXWlMdPhRZg9tF2iVBANer7EuiqRzkxj%2Fl1GaOXSy%2Fj66UGixuG%2Ft1sqB7YmHgrAZS4CCG5cVvoHKw8IDWDgif8TIUNt4CmGCFV8oRCaP0CmYYwpvmi6KrW0L%2BxztwTW5Q1VOtgEkM1SpHwl%2BpEtWUCCroov%2FQPOsDkYn5qLLmMFyi7VaYnVUsUhyEx68KD7lJ6Nc0Px1bUpg7ekZnwqKDvraFeogwpJg4qm1U0N97BWu2JxC0vqefGHjyYyTju6hht0T0r6O1aeVBr8ltdWeuZu8vSZ7Bm%2FD%2FT1eSny9m9%2FKYqPAXo9gWDDc00ua6HCtg%2Bv%2F1bZfvqQdJ4R%2FGfkgUkaNTAKuUkqei2AkAu4XAI0G1z0TPCcMkpi%2BcGNySdZtorZ1n%2F1XSt0IxjwyhbgVfiHbv3esUBspdhsxYnWkaTzC4yXnFb9CoLAURVO53lfW7855ltvt%2FcdXZbQkHy9F6FPrzrpyOGriE8FFzrupj3pr6B1hsXlUQLrFXCPLLdszOlc22dH3QocB1AsYdcp48yU%2FrWQHeh5aBNbe%2BMmg1lc%2FmYrOTrqNCIDubbK1lPT9e%2BZ3t0bFEQoFnMNKV4hgZIfb%2BMBYQDWU%2F7%2BBqpJQR41ScI9YJbHpmGilV1i%2BAe42Mp05c9PRsH2%2F6cwyFIaoM1twPfrKnKZF2Qi08S5%2F3S55HKpAw3jh1q6vi9QOxSvV1kT26E9hSrSoDXcctTOTnJj97lHeM9sw4TuH%2FLtHRLVhxPyyt1nUS83IZ2qIPS2by8WC1wlhPceZ1pkGor9%2BbzO5s%2FME0YWk8%2B0Qo%2FiprX1K6cl9XyFz%2F0T7DgNBKxtoUHJg3qoR8LrvlkAxOAEF4w4VhY1dBLP6osZVcfuWYwnUfjbrwHRNIkh50hdFYqLtUuHrsu4RcdzztnT%2FiTkCN9AJ%2Bhk%2FsOO74OlVFH8PAfi3%2BMMtJgG0ZiCGSxQx7G64tRBJl4XsqJ%2Fu00Fo0TEy%2Bzu%2FvJCHuY1%2BFiMH8pAMiCsmGvxSoTSY01YvgWZceGlRtY8BzKb3R%2F7I8o0OH4OcrwrtHNV%2F7Am4u5t6BtR1ASkmujioJ%2FldBYvAc3nnxf7Cw3UskRr6UOQJUq7HbOG4P1QSQqR30j7Xj9WGRXSXCBNmMIQdMJnh%2Fcwoc2BvmMYdjOAxb7gOtJOAmHs3VmT466D%2FwLgdb%2Fro%2Bmr5iASBs2M82dcX3RA41ixWZYz%2B1AAp3JJ8aNB4rrDYBYHiyiVv7S9ngVkEgxQs70nUS0x0JGnTVISnoVtMvSoFu5qrQfuZ2G9ON99likA151EvGMJtIA3dOJc7m68oIL8qsca%2FAelaKh54dyyFej3sHJv%2F63%2FqMfmolcq1DWGreFDCqH3zvKh9aZQUKce24WCwUAl3Zf5SuorI1SQGftzbJ13Knrm0urmc3QkxU%2BfqPI6llNDHYJLrSEsjqLVLfVc7NiWDWSY6SuI1MyGxzVgaRQUJFrI5bTsnvLGxljfL%2FHnFmBJ%2BHIWxS7QnNuRJtKifnHLzlQYFBCVfyP8eabgPHB4db01tfuXA46eZTg97OACK7s8SAAXK5yYNtfMz9IdOWec7X1hubfjfoLKvJRSP4yobJ%2BUbXQfKQZYcWDdewDD62LKDbtNun52VuwQ3tkThoBLlwdFcUWbwJNAfM2JPFcRD83TQapD0ssV2AhhtqfRFo1qfQbdJEJNwNrOBzi6bJwf%2FBGuBHCBI8MBGWYR5aQXHNbGB2pHIySTRO21U8yvH0ILsBzgH9hAwULnYwPzX%2FqFqhYD5JXalsaFNXJmAyENkT3I5Kpi%2Fu3exsklSEm6uDwMIa8KH5kux4%2F%2Fy1tgRzLxB3hu8mpIoYRYhhmLDy1g99vsGvDM23C%2Bi1U8ZtxBZtIstesHz2IfeG3gKnWoPt3GD44FmcOtMOY%2FoYhv3e%2BjHeSycn15HDMcbFPSAxGBhHPEayW82sxrKg%2FXLq7HbMMI8ccng46LzQo3%2FRx95FjvV13un4oGknjnpDybt%2B7yXp6AIpmpoXkR0ma9pghhw6qMU4xq2NyWwdaPZaJofyskyCnSBYWWZzINM2CBe2DITXYd8p8%2F4UvKriEXJlg0weJwOPRrbg9FvU8WIAjc725zooDd9griNkYox%2Fzh%2FYvVxWg4LZ7l0MLNH74S%2BaUFzlmccFyRqUlbpffoAqXk45MneCZwf9esGltTLpxFXaBs0h%2B0neLD4lZVsXPdpzYiMPFYvvSO6c6NO5OaVzFQm70Tw%2FR8UjqkYI9JyTwAwmun5g57aSk2b1h%2BOLgZ2CXXcfoi6zYvNKnEcy9woclYOE21cze1p%2FVqAQw18PfJ22ASbXRH1shG3h%2BFmd24RibexDe8p8Eux%2F0IKDEtbmszFH%2FNIcklKrVKDwGnYiddI2deMSY%2BiR3JXlJtGjBhfFxUQtb45bK9biSoMiPwQSvTeXYx045GulOFNv9XI%2B2lTSjGilKeIPf4H0bnrfU3VKa2bVQRoQCNHtZamDUyQ8Ta28Li1kj9XC1w4v1jmTmmuIZ4u5Ai44ht7aUmDGAA8xWV0mi8Hb3WEZq9POrrQ0qOEGz%2BhyzBeJ%2FU0IGw%2BHmS4hoJdWqNLA%2FZlgZ%2Bh5EJXCPaP%2BP%2FQmuxp%2B7NsqFwVPtWaUXrrIr6GASBLJ0m%2F5gwFowN2Uf2mUHuc6e%2FaSSCRkbyjBfQeOyi41C7wHbfl6BfYFQ90iW4HZymgbdEz70AFehY6rCy9cn8uJIeLzWGnBvuRvFvOr04CNWwhf15w2jLs4wYyCQI%2FicNtk6pq%2Fg7LKH%2FCQG0ZkufwvtS2aupSBAfr9VcTrsJj%2FFI87G1aqMEh5ufR3rnzJQswLZXulBI9a8QU27PmIavd8HlEIoCC%2BvbO%2F29JnrqPRAUMhoHtLqTaTStR6CyqCE2bxGudIsaP3gg0KWy3ZVs2UoW39zluUp8nojDxlupSRNYuvuyM1Wu%2B6s3GMBolVsBoWnFcd1XdMrP3xskDwmcOTzx25IA7987HYqLChtxxJt4gUUAuHU6r03WbfhSKSNqFxLEDXfcYOmuFQkqcfo8LAUdnt0GPW53PlvdK5gvSpwpERRIklnAGSWGWOD9WzjlY3VE%2BgYtFhOXsqoHJErLPZ8wdlqkQmMHi9YFKCwagzim%2BOha%2FasW01cJt0QvL4dSUCGiGgAM66ZyOQ78g%2FXViM1F0WS06UZNa3j%2BIDmgcHUpe8evEUdSXhe3CSZXVyIZyNfpGf%2B83F389SRnc1826UbKlnzMhh4NsPex4NZHBEgP28yNaAMgd6fvW8DFXGn%2FWwCfsB%2BSwvh9j9vGE3IIglQ2da2qTjJ5l%2BUCiUxfqVYz8IBm2swGVZ2BaNgA1raK7njLfZ4TAqrSF0qL6AThTsPKMoN2J6gGBLN8RldVlSsEBJ4wmxOJCdILTPeEAlEBAGuh0kL%2BNOlNJjoJVGxeqrBV47uxt5VOTlF93Y5gA%2Fd%2Fv9jlS2lOO4KhAT6NyBFpDNNVLp7jEdbV8SE%2F1nBg2un%2BbB09Gt%2BNPRmMoW0M3EbIQ0BNBC2CoFllYE7CZOYM9S5mIWlc0DVlwF4YHRfVEBdIiugXE2G1%2BGN6ynsNZnJIgg7064u4%2FbCfSrY%2FD3E4vzpWrmgobNo5ARpQuUkpFmgdWagA2CC6C2EjheL8JJUPM1x4oFyJQYtqyAq2xTUPMXUC6OvbC8sLuumHRaZg097JpJLwZi90m4hfBq0Pr2YesNyMJJUe%2Bb%2BTfuMi9mGw%2B3duC%2Bw%2FOcB4jGIOzt63FZF5iOHc44n%2BULkt7W%2BH6dJ8sxVzJI52cJzJo2M0EKwani%2BQuRp12D0xNiPRs4fsSRMUIv8hu%2Bk62Z5E9S72dD8K%2F2Zo%2BygLq5MKIkhxp09QsxT2HGrqWLruKg75wgv5jXnPhUFAcp71wzjq0iNkD5aUTYPLhnVN3Fnq9dzfI2bx4uTwpJ98bam0CRjsQz3i9P5vG27iy7WiOhntOwhtFMbUm4MWwSQ02CmQfreg30zlhZlE2EpCBBlr02629ozo6MbnK%2BL1knf2TJqJfI%2BdnJaMVW0ETFsule%2B5UfznDfQqSJGw5EQKN4HMHjzgSJi7hUfThRF1Dk5QndcaxnoD%2BuqempNmvSpv3XAoBx8ZxXRqg4gHcsEtJuYrf7YGEqdbeIzYW6FvUqzhCfHxvrHYGfU1zdj9TAhy8zuloCBj5EXnZudE%2BSNy2SzJEeAf%2Bx4pHkmNqlV75QeUVQvxhrPmHaoQEZhTAP2IGOmnunGBn3%2FyDVwTmA3bHGH106YoeBX%2BeXNId3kO%2BfafhGowUiFxvTZ07jl28aMnf5jaWNIETrdfLologM01iwJQs7sGrQeB0H88b63Wu8tguOkRGxvmo8fugvATZpYe5%2FovndOWtBjJ9Xdx51ZE0VmNbBaKj1yfDVErJO0IZK6b73Bt6WIugMaUzQ6tPYPjeabTLc%2B5t4i%2Fa%2BN4fdsxrLjwft%2FovEPoJdHFeRU2zJRtOVk0AcaKI2ZgMiRAGVnm3bzsPIxbtynEo4wkYZV3DkZLqnbRaZZujYuwwaoaN%2BqYJfLAMBAswDm7ubOopioyg%2FLoK7mTY3jJU%2BEtONS2wJZivbB40zVDtWK9i6VpcTIfMOrNsxsctMcTBfkMiGRiThbcumLaTVIKJZZ5ZbJdh7dq4EYnK0KjnFEwjSOhu57GhRVDpW8LikjtwMAcMw3txBqcmWpoMn2r4wc40nL%2BIx2dmga%2BF%2B%2BRTZ9gBqRET3TczAoTKFtayHXEFo9m%2F0zTt7ALR3guxDXACPalAiacBQDFGX%2FikjNLt8AJorShWHIsm1pv9rzGdLy%2FeA0mF3LhnMetJ7%2BkTkAjeLZVDpxuXbV%2BxZI7%2F4H8gG19M6TDsY%2Btp3H0oYNo6mYaSk4ib%2BfVPVBfNBXPcslBEj8F563TqPaGhIXJ0QldAoY8Mr%2BZzAx6fdwmgSbuKgWwQE%2BEXocHNbCSeGkL4Bmi5JMFyy4SoA1NZnz3ZBXm7%2FiAdTWiPGLY3RWit5xJhsP980SlXI%2FRhPFERcmMioonOD1%2Fj39jtQ29mV3FFKCCWDRFwwkFHdRKRt6fELlTqjDC7YRZSY%2FmNi9XhUjtUswFuzyvpki%2F8q2%2BDTWadPbeice1DczE0en9AoSFxD93qFgmHKyJfGKPAT7qU1L3Wve8oLsmRdkNaKTgFGoiajZ3b3kUVUBtqecUTv72Wj6Y1qTmRVPf%2Bw%2FhCdii2HIl0fiPZ37ogpcNn5UcCXNFVPN8B106APKQBlw8X4nb0%2B6jvg%2BUv16QonsCpy43JQDYrK1eoGGIrHcckGnGJfkltiqHnYBDDrBgJlmlR1RS3inuhneqnxFIX0bw6%2B7Lw92Uxl%2BtfgHti4T1lSuqhf6YCsiWk7LDMJZXnc2so%2BGEbOpemc%2BxyupDA5qWVk5WCAF9epnzxrN7bIMciuHfRqAFXNZL9DG4Z7m8FFUUk4nihPz%2BxbtL3f4rmpb4QdWbRIEVUh8mCRxt09RKMFr10nTV9PScdtPsOl24WK2UjST5tJe9KsRdJLJSgijanKqeHMBrMZ%2BnaQxoNVwmtHXyjTYOpVckXTzYsjz5TncP3pVXNReUehLfDB3KOxNfbSAFzJSEtXilcoQDfZE3VGGMW00tLc4zc5uFlzffLNNlV5vzqMDHgZhJ%2FfdoLpjQOW9XBQuFFmqjnpnytPYD21g6AG7fuJ6t5RS5F3ojMmw%2B6u1JIf3IhHlCjkEtt4ndEcyGm8HQXPbpaQcNOk%2FDX7cVGFI9lwcN4VlNMAj%2FeSyZw2F9Ya55O61dV6kfqKWUMbWM6SdcRCPsWa6euB4LuZ6sBOJmCpS%2FNabtytMkJRWNPPsurPPbHfGRD4LG73X1BPhi%2FpBKmOvTyGIRTWY2NbKfNGK5ZaXcuJe6fjGTaNufIeFOqq6%2BOchh2N9BoDyXcCIrWBb5e%2BjCKYKYVZG0ZUEaNJLbUKxhY6zw6B%2FdQ1okj7jMBgugZXMy1LydKjroQwye3Bwn%2FEf%2FL%2F8PFdqoy2uUa0k0kwhNF%2F0hxyjyLUPKupD5r9KV3KmFQ0bdf3MT3AtSUWzlin9uwEexfAkJyf1U3RtccK%2Fy%2FaIYnpfVnLWC7a2x10wMF6Bqn8S%2F8JHTHulVORJdXksqvZ5rZafFyPSXS6uS4yeIymSPc572DIEbv06I0dPmILDWF3UUH1%2FZLV5ow2ZBHUHWZvl9JRk%2BkvTO9WhfYgLDLizX6abTcXjWXT89LfEKtC66A1TAA2hPcDgDjENKnyo%2FTjs3JV5WXJBUUca8U04Z6JYfG85XZxbevWOJKLBcrIytZYcdW0pUdcSNcLojF3SToiCe%2BTbnGGEXDKhIO4q9zDdNX8fB1qrZ7e6T4bz86t7MsDe3oFCOBkbVkw2elgUaBwzV48gxvqghZhsqrjTdoGmYLp56o2j7YcsklQLxr903pZpAzPNzkXNVYiVxAAnl7qH0eCzX7V8I0BvuX5QbknqwVudc7o4KMU1nv3mOrz3vmfY0%2FLnjeO1nNJ0CguXumeTZ5GS1EMq53nPKj7LatMeuGM%2BhiYE1Sx1DPWpOev5%2BcY1V8sAx3moHpPUmvILvJQHDwU2MkK8rnWdBUbj%2FB%2Bk2dI1UR%2Fwt2YUvChwGW%2B4vX7L%2B64ceUhgaT5S%2FaUhud4ejKFQEwUJpY0lF%2FBrxkztIyIRFipEA7HkstKUYfXoHcht4fJVjOaELjCWYsZXBe8MT4FETLtaaUKqLzdycGwQ0ZqlSglzM1eAoForQ6PuTZ72h20YrEjdlP%2BV9PZBcYltnWJb2%2FhJR7VGU2nw%2B3m2D%2FRKIPfRQ5vvt4jLcLP6%2BviCIGj0EVuXyBiC3%2FzIRJfL5OGC90NRQeMUT6UrQ%2Ff7K%2FGmOEwJsSXaODVqqZj1vWX20yjl96GiKA5LSW%2B9ixQXkV7gwAcSH3TI70Mn5Ry9%2FCoy5ZAp%2FvnpsVA%2FfXdhY9tFTSeMUuszqEb24KwfMZWaHXrdDspe%2B4OENoGcrHF1UhYINMuGb62pjQsMwbqGis8q4C41b18o0tkmqM0UlTOUZF%2F0y9pSe4aGf4MGffK%2Fml99mj67C8a8ItESldXR0wEUg876pUAVPhMgVjPvUP5Zw835ip050%2FGeTawEJ0Wb%2FlAieLKOYLFgRwqaBE7rxVyJpHcxCpOaP0yEBnwfYH8my5a7hd5kPzoScTzsqUdXnuVY17QhtxbbU1N34A7HAFtEmjeaRk3xYV7EBbRMT03ssdsUMIfGgUoR7N7tGwNaLGltkOx5j4NCB47mpvcW0pEuiU8zL4opnlp1%2BmnE1oqWw19MVDtlYYjIP1AC8ISRC%2BS2N%2BgvmIMEC%2FHCzwk3lR%2FuJI3UqxDxZmoSNULuYu08e6an9RThU1%2F5pzE6%2F99GB%2FcW1SXjikV2JIdc9O5xKqHQvArDSYZrPXeifq%2FZs5NBv3Qp%2FMgZQNdvSr9WmdGwkgyzSNItYtD%2Bx6pBBZRyD12JVL2WaWLgFDLdtRUvF5Ig1Ke7ISWuUuwbEwKtq2mIlhdjCm81AphG1FD6oYyKOaiZtdcyDDSJY6ENFRpbF%2BDNuSiFq9Qwo6Iz1Idl7lOODrGSjpxVImTSTjW2L59NlRbjtRGi0WEQIB2qckwZ7TzK4pfWzj1H7o4sZa2TMYpRffCm2hhIC2vj6%2B%2B6dCdlxbqxQk3a3h%2BxVo0W%2FxjeOLLADJhpktjnWF0xLIceuXJ%2B1Zw40NAwQASxJwwbIhzs24FQPOrMXisfrpup8BNRqHB5w55kHRYQ2vZy4iOsI4oDODwCV4EpjZ245T5LHd7EEUrtxsvg3EIJfYN59uh91z2vfArjLSRs7cWUeN1J%2FVwnrKfC5J8maIcLRyhhwASunnZ9i3HuFgUFPOGU3gywf64RrvbSxkY5ZFoJIrTHQZnh6upEBjnTXoYWzYxN3vkp45yymB5U9YlpB0hr4a8zNPnxxifYh1pWpciZDeuy7JQHMyt3PyFq%2BY%2Fiqa6oZvtTl8s6EoREwrCilvP3O0f6xBInSmNJgt2EjUzpUQJyGgt2OwXnF82Wpg%2FP56TZDB0VAEtlMNHWaoVs4VBsm2TyEueQ7kb0wabH50SBUY2RWdvUT5VVyPwISeyIlcS1x5ang3dvEVmX8fLiQNch81D8LilrIgqtLZVT0pPbRhrIDXF5e2j80f2e3%2BZAS0hmaMKK0XHp2svyZS0XR5exzCAv8KlkKEMEbNvSVs4Iu9ciiXMgd7xCz9V%2BaiKlMl0gmtWqX1HdNeMumxb5jniA7h20E6kGGX0D6KSyCWvD3pFUcTmCstzgEinsO0vDglYzjBcYKeOHnXfenAqYDUyQ9%2B1e7JNdtS8O8sRDPtcxdDB3Csqgm%2Fd7OS1QDFXMgZpi9phfRzab9i0pkuldtjqA8J9XW%2BxL9K00iK0gsATMFduLv2Q83NYgs3IPTmN%2Fd%2FHz1J3BTpSDWHkXHG7V7fva2%2FAZfzZ3kShlGa1Y3vO3TG3rTvpk%2FpbDIaLP5Yfo2%2FfZZhU7h5lwGA%2B9NVIkD2lvfEg40L0%2B4ny%2F13Sxx%2FSuQUKLuBM7ymCZ8PifZ6eUzTIz8shYFhIqdmMFq%2FGbdjtrQYOl4WhP30KoopbVnZFbDNCGE2EVlgmWSW8FwW%2FS8EzRzWxQlw%2FMk1uDkkUGrgOKini6ekgSaYZs%2BxrgnEXT8OL9Es7FSYhcyXdFtHXy%2FudAFmto3rLGE%2B7qezLlyc0QXyEoZ6tdHrU8khv%2ByjH0GiZyyDehIFZN%2B2TNIyLwDu8Ux%2FGTYYNTEgkQ0wir7EFsRFj0qj9%2BrCXwTnDjCPV9FoYf9BohgSh54bKRLwgTmG%2BBBxMRdNavF6hOfy%2FT0VNSF7gKWElpJYJmyKZ9MxCTuuyDcpmZfsrzMVF%2BF5Mm95g2f0HWHZ049PV3%2B8Ju2upwXq%2BEykPXC2%2FKk3qjT4JId7wvt8KTRllpvOtGqKHmparAU6hjwYNO6LW77%2BUWJsSWV%2FgcsI9a72V0XKXAE06cA1gR9hVX%2BXJ%2FpcCT9mlcIz2kZfUPs3PC3G5LnomlV6RD2AT64dyPTwz8INAlxD%2FVkA1iPm%2Blwx%2F3Dt9A%2FwQQgG2x40Bq5Rn961vE9ry1vxG4UVoyUQpL2rgymFOA0jNkI0Ij%2FWxk%2FSJaj4RVYCEKlS4H2P0JPE%2BIDeXFwi1vbLNKEXSfy2o5gDh7ntz4h2eq1LZ94Ncdj9NYSsTW5A8peMn4rHdmYtuSAw%2FMLY%2BdIjGWFqf00VTS1Z9e9D%2B8B%2Bg2%2Bp4vvOQdsd9DNlzker7S8PEdlDFYcn9A5%2BwNH92I7KDEl%2BtGX3QGOsMCOSScocASoxEWgPnuvejIuoSuW%2Bn9kGZGe8qdDwyYepRkD0kxyj6ZnBB1ylngY8WA7qLyCAnsb6vf1tSoNTMSWQ0BDis8y%2Fwi9gteD5EerOKNIg72D0%2BQkJDgQPmmecA%2Bt51k96164umTVTvl9nAJv%2B%2BLzK0w%2BKSLKrvT7qJe5zgLkR2uB6awkJc%2BHTUdPL767hDfoXAxr3KyhXAPnItolj5%2Fji7iD8Hlcr7eGIQmMC08ROZ2v9b9Xohsx0JfN1%2BCUcYhG%2F%2B72DeJvcqB1btzWUAaOe0BEtcinzgoM4lkb74qdSU7tChkyqUU9u6Y1tuM5wes%2BQluUT3cXamWyY%2FmDd1G1J5gVIbmMg%2BsGDabLGYfUIRp14pQIfUfsOyMv9IyCVR%2FLrVhoH%2F6zHcAAicPShYKlyup%2FxcGgsxOH%2BD4TaK1prtzhw6errZ8w5ln4vqn817guQs76yZn9bbQI9DfJk1zf8DVHXiADBVYdocRpGpp4QWLnb88sPXMEt6Uo%2BVXIMUjFE3z5dmc%2B2k%2BVSUm0NuI5lS9ppqSTedRJcSNreMtk73%2FWGaBysqDrulZIHzJyE40xCAUgmX8FwitvCnFSQXozXonXir0JNmlAyjTQnrHG77TOTPYw%2F9SOqwieyi0dRvCRWDgm12NXlfnT%2FrtWZXBedRl6hNTU68BYEXBTj0CDs1jUFyeS%2FU%2BtM%2BKx%2BscqK%2BxMbOqn09YIyjQDhDJTqOL77o8d9D8VGpZHPFyg1s4bAzeK1Z21mWy3YPKh4SrZC6SpHYX6Va2P6KFKGicdE973bhgLDE9Y%2FF6BykJGcIhealL5YNRUxkgwMAHUsjGToqpCgLc7IlHRFx3rfXgjvt44gRGJ%2FqTyBQXQnLxglfI5cKYHon2lWj9WdZPsFWNuDgSrdz747W2okc8coz6tbHKNcR3Isg%2FuMDBOe1AdUGfdUACFdVmi1TOSzMAxznWPvFGBP%2Fs8yUeXXNm1lIwQfm4vrYdsbIx%2BR%2B%2BNZIHk7rvLRrYLYaYRwFpDh0%2BfE%2B3lhmNLx6LioEerk0ZV4jg9fXHfCgxgoRrxc2R6N117FJmwdSuOGWVMF0PuzM0tGp8rCeL6TJXClNrE77qU9KiFUR2nbQY227oafl3%2Bdmlk1knXq2ZME3epKB26sWcauj6Y5PNUoz7WeCwvoDc1wdUrZ0vcOHLsz655WKhFGo6Uoh2LomRn80SHisZnL0aujJLacyD2X%2FocZXMNk8yveog94hEancVaxjv6uq42qLnlWD%2FQQFYHjV5c47XOUWU7D7BJi2xT4YxasZ5wdHa6ekO88mTZT%2Fj5SZXGSZaQLO910RSB93Ztqd8dRePkIlw9C%2FzJuDm9voqGraO2ay30h8zaCsOSjKiQKnnS157uCRqtDjyPLDGjzqSkYGmweDBlh3Q%2B%2FuMa1ygIW6fqQhegKxD4w7wPF7EXdNIgm%2FG7fgTvqkZx5vDy5nMNunwubGDFe24vEyT2jLl0gF%2BDXJ9FiBxzrN6SOyxTj9%2BQ7JX3p%2BWBethKsVRMdmk8m6nvWFP1dwGg%2BQi%2FEwyUDUJrHGQaWDydqnKosrXGEy7e21oIh%2FcE5uQ%2FFQVXELWoV9rGKZYVWFH6L8dh8DwNr24avUsXX8F4UlfG3z7u3gzTHcRD11xLnY33amODeZLJwgdEo4bVEWIp3IpKuchJSFoKQs8yWmfiRicRsIFMW0Qh6G0vh0v7uOpI68TDq6bpw1iiJVNQ%2BHg7PgYtmQ388jWOplEOHbbNhXLEtifU2haZNdQQq%2FdfibsVSM%2BNN%2F2frsDCo6V%2BhEr95xn5Wkk1ZJayFYO%2B4e%2FIhcf5pDjQXk0phD9IB4nVypGUTN6BnSPQJ0HK6xcjmCOQ01QzqoWsn9YZ5W%2FSlRzh4jXD%2BzwBYezWyCDNfkjp2ktFav0T2g0n56vAkDkn7DYQBK0u7SaTzVLuUHrWp0mvQ9FNl4nZGEq2uGM6Ay9rj9EazfA8E%2FcYyyjUmHOa26fyPfyCNJLAQMNPkUDnDU66koGG1OLqLTvsSwYdqspC%2BWc%2BpMdCPHMogLbKSK9GEk5dqvv%2BpqYDbwp6hpLNDUaJGe6AJaN15LZyKo7rOI5tFs5Lviq4g%2FtHnmWA%2FYXsL5BBaj%2F0iZPuoP6zFqsnOZ4EV%2B35BkzBqcU%2FrEgfPmOHR%2BZLOOWZ21SkdrRau3JZVdEQQIlIJpcopax4%2BwM%2BIxPlCqbh7%2F0eOZzlEj8ymiE7%2FkFEHPnuJhQpRH03pmTMEA2%2FfcStagxNJR4YxeQLIylaQtMqrev1xkiv604dhZjaNkZX3XMm5472wfm%2BzCjLsLrVWaFdYtcCybLOiuB%2FdZDOYwr4BSKp9x6m%2FXXFRXesjL95PsfToOgPbI5nYLIajgVSQ%2BOhUqHw3y1bkvZzW9zj%2F6gxp4u2Kz6hH1DDqhAU6am3bwD7zPXbIYypqew8kOIGD6IM%2BUJbyLmvBo66l2x4ma%2Fo1hGp5LjPoTGeNtXNyBgqXzDf%2B%2Fe0FE1F3Gmu4XmHW7p%2BHUPoC6%2BuWD2%2FkhrW6XYrw%2BB%2BxUGDxSegSrAV7LsUs08wzLI5JTQf8GYv1XPVcuNqVglFzmFuvsUIEiJvEQOMJpLRn0%2BqQe4uH2vxNcTs4jPPIwwYdD5%2BkCU8YA2YKRRXQbqUDjWwsLYiArmFcsTGiZPSzrd%2FFsu%2BzRkfiC4V5lFNsRp0m5hd%2B8fso1YKwIIwoRxtWnSBF5ybGZSf7SO5Fo6cWEzxXWp26%2F%2B896%2F1xXc33ij6%2BD8j2ptxjsXVFQCu0ZBRd6HYcEHSD0LcWXa8rsbzPWa%2BQPTfaA7By%2B9kcSLXcRBRQCYHWV%2F2DXH3qAtdIv1s6HIZ12ReEjOSojKPirG2fMc%2BCiW5XVhgHEehvBck1nPdqFV%2BjGK7KL%2FPZdcvc%2FesrZ8WHFwjUB9DY4U02DKa9Hp3r9Mo06yjMZpZGdIDEAOJ2FaaxnT5uOBthKoebt0PR%2FMr5xVfhKX1vfDalBr9cVQ1Wv4pW3B0BL%2BPzg1as0BkI9YtxCkggIYG31GpIJzg%3D11wgs328
     *  同包下面的图.
     * */

    public void sendRequestToServiceUsingEureka(EurekaClient eurekaClient) {
        // initialize the client
        // this is the vip address for the example service to talk to as defined in conf/sample-eureka-service.properties
        // 1. 通过client查找下一个eureka服务器了.
        String vipAddress = "sampleservice.mydomain.net";

        InstanceInfo nextServerInfo = null;
        try {
            nextServerInfo = eurekaClient.getNextServerFromEureka(vipAddress, false);
        } catch (Exception e) {
            System.err.println("Cannot get an instance of example service to talk to from eureka");
            System.exit(-1);
        }

        System.out.println("Found an instance of example service to talk to from eureka: "
                + nextServerInfo.getVIPAddress() + ":" + nextServerInfo.getPort());

        System.out.println("healthCheckUrl: " + nextServerInfo.getHealthCheckUrl());
        System.out.println("override: " + nextServerInfo.getOverriddenStatus());

        // 2. 拿到eureka-server的instanceInfo, 开始用socket和人家连接了
        Socket s = new Socket();
        int serverPort = nextServerInfo.getPort();
        try {
            s.connect(new InetSocketAddress(nextServerInfo.getHostName(), serverPort));
        } catch (IOException e) {
            System.err.println("Could not connect to the server :"
                    + nextServerInfo.getHostName() + " at port " + serverPort);
        } catch (Exception e) {
            System.err.println("Could not connect to the server :"
                    + nextServerInfo.getHostName() + " at port " + serverPort + "due to Exception " + e);
        }
        try {
            String request = "FOO " + new Date();
            System.out.println("Connected to server. Sending a sample request: " + request);

            PrintStream out = new PrintStream(s.getOutputStream());
            out.println(request);

            System.out.println("Waiting for server response..");
            BufferedReader rd = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String str = rd.readLine();
            if (str != null) {
                System.out.println("Received response from server: " + str);
                System.out.println("Exiting the client. Demo over..");
            }
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

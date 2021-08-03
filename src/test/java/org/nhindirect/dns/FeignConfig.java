package org.nhindirect.dns;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.codec.Decoder;
import feign.codec.Encoder;

@Configuration
public class FeignConfig
{
   private ObjectFactory<HttpMessageConverters> messageConverters = HttpMessageConverters::new;

   /**
    * @return
    */
   @Bean
   Encoder feignEncoder() {
       return new SpringEncoder(messageConverters);
   }

   /**
    * @return
    */
   @Bean
   Decoder feignDecoder() {
       return new SpringDecoder(messageConverters);
   }
}

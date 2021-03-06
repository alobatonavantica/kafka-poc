package com.co.kafkapoc.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.co.kafkapoc.dto.MessageDto;
import com.co.kafkapoc.kafka.basic.KafkaProducer;
import com.co.kafkapoc.kafka.dynamic.KafkaDynamicConsumer;
import com.co.kafkapoc.kafka.dynamic.KafkaDynamicProducer;

@RestController
@RequestMapping("/kafka")
public class KafkaController
{

	@Autowired
	private KafkaProducer kafkaProducer;

	@Autowired
	private KafkaDynamicProducer kafkaDynamicProducer;

	@Autowired
	private KafkaDynamicConsumer kafkaDynamicConsumer;

	@PostMapping(path = "/produce")
	public void produce(@RequestBody MessageDto dto)
	{
		kafkaProducer.produce(dto);
	}

	@PostMapping(path = "/produce/{topic}")
	public void produceDynamic(@PathVariable String topic, @RequestBody Map<String, Object> body)
	{
		kafkaDynamicProducer.produce(topic, body);
	}

	@PostMapping(path = "/consumer/{topic}")
	public void startDynamicConsumer(@PathVariable String topic)
	{
		kafkaDynamicConsumer.start(topic);
	}
}

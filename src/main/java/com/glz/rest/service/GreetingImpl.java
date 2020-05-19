package com.glz.rest.service;

import com.glz.rest.model.Greeting;
import com.glz.rest.model.Person;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingImpl {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    private static HashMap<BigDecimal, Object> list = new HashMap<>();

    @RequestMapping(value = "/greeting", method = {RequestMethod.GET, RequestMethod.POST})
    public Greeting get(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, "It's me GET!!! "));
    }

    @RequestMapping(value = "/greeting/post", method = {RequestMethod.GET, RequestMethod.POST})
    public Greeting post(@RequestBody(required = false) Person person) {
        return new Greeting(counter.incrementAndGet(), String.format(template, "It's me POST!!!" + person.getName()));
    }

    @RequestMapping(value = "/greeting/put", method = {RequestMethod.PUT})
    public Greeting put(@RequestParam(value = "id") BigDecimal id, @RequestBody Person person)
            throws Exception {
        if (id.compareTo(person.getId()) == 0) {
            throw new Exception("id must be equal");
        }
        list.put(id, person.getName());
        return new Greeting(counter.incrementAndGet(), String.format(template, "It's me PUT!!!" + "added " + person.getName()));
    }

    @RequestMapping(value = "/greeting/patch", method = {RequestMethod.PATCH})
    public Greeting patch(@RequestParam(value = "id") BigDecimal id, @RequestBody Person person) {
        return new Greeting(counter.incrementAndGet(), String.format(template, "It's me PATCH!!!" + person.getSurname()));
    }

    @RequestMapping(value = "/greeting/delete", method = {RequestMethod.DELETE})
    public Greeting delete(@RequestParam BigDecimal id) {
        return new Greeting(counter.incrementAndGet(), String.format(template, "It's me DELETE!!! " + id));
    }

    @RequestMapping(value = "/greeting/options", method = {RequestMethod.OPTIONS})
    public Greeting options() {
        return new Greeting(counter.incrementAndGet(), String.format(template, "It's me OPTIONS!!!"));
    }

    @RequestMapping(value = "/greeting/head", method = {RequestMethod.HEAD})
    public Greeting head(@RequestParam BigDecimal id) {
        return new Greeting(counter.incrementAndGet(), String.format(template, "It's me HEAD!!!" + id));
    }

}
import json
import re


"""Restructure the input of cities of countries
"""
data = []
inputfile = "world-city-population-scraper.json"
outputfile = "country-city-population.json"

with open(inputfile) as f:
    data = json.load(f)
i = 0
out = ""
for country in data:
    country_name = country["country"]
    for city in country["cities"]:
        city_name = city["link"]
        if "%" in city_name:
            continue
        population = city["population"]
        m = re.compile("[^\d\s,]").match(population)
        if m is not None:
            continue
        else:
            population = re.compile("[^\d]").sub("", population)
        city_name = city_name.split(",")[0].replace("_", " ")
        sections = city_name.split("/")
        if len(sections) > 0:
            city_name = sections[len(sections) - 1]
        out += str.format("\"{}\":{{\"{}\":\"{}\",\"{}\":\"{}\",\"{}\":\"{}\"}},\n",
                          i, "country", country_name, "city", city_name,
                          "population", population)
        i += 1
with open(outputfile, "w") as f:
    f.write("{" + out[:-1] + "}")

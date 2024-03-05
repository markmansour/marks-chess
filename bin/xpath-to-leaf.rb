#!/usr/bin/env ruby
require 'nokogiri'

# Check if the filename is provided as a command-line argument
if ARGV.empty?
  puts "Usage: ruby create-query.rb <filename>"
  exit 1
end

filename = ARGV[0]

# Load the XML file
doc = File.open(filename) { |f| Nokogiri::XML(f) }

# Extract values after the "pv" token from the search-summary element
search_summary = doc.at_xpath('/chess/iteration[last()]/search-summary').content
moves = search_summary.split(' ').drop_while { |move| move != 'pv' }.drop(1)

# build this xpath- /chess/iteration[last()]//node[@move="a2a4"]/node[@move="b8a6"]/node[@move="a1a2"]/evaluate[@move="b7b5"]
# Navigate the iteration and node elements based on the extracted values
xpath = '/chess/iteration[last()]/node'

moves.each do |move|
  xpath += "/*[@move='#{move}']"
end

# Output the result
if doc.at_xpath(xpath)
  puts "xpath: #{xpath}"
else
  puts "No node found matching the moves extracted from the search-summary element."
end
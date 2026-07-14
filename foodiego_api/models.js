const mongoose = require('mongoose');

// Food item schema matching client Food model
const FoodSchema = new mongoose.Schema({
  id: { type: String, required: true },
  name: { type: String, required: true },
  description: { type: String, default: "" },
  price: { type: String, required: true },
  imageUrl: { type: String, default: "" },
  rating: { type: String, default: "4.0" },
  deliveryTime: { type: String, default: "30 min" },
  category: { type: String, default: "General" },
  restaurant: { type: String, default: "FoodieGo Kitchen" },
  isVeg: { type: Boolean, default: true },
  isPopular: { type: Boolean, default: false },
  timestamp: { type: Number, default: Date.now }
});

// User profile schema
const UserSchema = new mongoose.Schema({
  userId: { type: String, required: true, unique: true },
  name: { type: String, required: true },
  email: { type: String, required: true, unique: true },
  password: { type: String, required: true },
  profileImage: { type: String, default: "" },
  phone: { type: String, default: "" },
  address: { type: String, default: "" }
});

// Cart item schema
const CartItemSchema = new mongoose.Schema({
  food: { type: FoodSchema, required: true },
  quantity: { type: Number, required: true, default: 1 }
});

// Cart parent schema
const CartSchema = new mongoose.Schema({
  userId: { type: String, required: true, unique: true },
  items: { type: [CartItemSchema], default: [] }
});

// Order schema
const OrderSchema = new mongoose.Schema({
  orderId: { type: String, required: true, unique: true },
  userId: { type: String, required: true },
  items: { type: [CartItemSchema], required: true },
  totalPrice: { type: String, required: true },
  status: { type: String, default: "Pending" },
  timestamp: { type: Number, required: true }
});

module.exports = {
  User: mongoose.model('User', UserSchema),
  Cart: mongoose.model('Cart', CartSchema),
  Order: mongoose.model('Order', OrderSchema),
  Food: mongoose.model('Food', FoodSchema)
};
